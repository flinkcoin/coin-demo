/*
 * Copyright © 2021 Flink Foundation (info@flinkcoin.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flinkcoin.node.jobs;

import org.flinkcoin.data.proto.storage.UnclaimedInfoBlock;
import org.flinkcoin.helper.helpers.Base32Helper;
import org.flinkcoin.node.storage.ColumnFamily;
import org.flinkcoin.node.storage.Storage;
import static org.flinkcoin.node.storage.Storage.NULL_HASH;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@DisallowConcurrentExecution
public class UnclaimedBlockJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnclaimedBlockJob.class);

    private static final int MAX_BATCH_SIZE = 10000;

    private final Storage storage;

    @Inject
    public UnclaimedBlockJob(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Map<ByteString, UnclaimedInfoBlock> map = new HashMap<>();

        int count = 0;
        try (final RocksIterator rocksIterator = storage.getTransactionDB().newIterator(storage.getHandle(ColumnFamily.UNCLAIMED_INFO_BLOCK))) {
            for (rocksIterator.seekToFirst(); rocksIterator.isValid() && count < MAX_BATCH_SIZE; rocksIterator.next()) {
                UnclaimedInfoBlock unclaimedInfoBlock = UnclaimedInfoBlock.parseFrom(rocksIterator.value());
                map.put(ByteString.copyFrom(rocksIterator.key()), unclaimedInfoBlock);
                count++;
            }
        } catch (InvalidProtocolBufferException ex) {
            throw new JobExecutionException("Some problem!", ex);
        }

        try {
            for (Map.Entry<ByteString, UnclaimedInfoBlock> entry : map.entrySet()) {
                switch (entry.getValue().getAction()) {
                    case CREATE:
                        createUnclaimedBlock(entry.getKey(), entry.getValue());
                        break;
                    case DELETE:
                        deleteUnclaimedBlock(entry.getKey(), entry.getValue());
                        break;
                }
            }

        } catch (RocksDBException ex) {
            throw new JobExecutionException("Some problem!", ex);
        }

    }

    private void deleteUnclaimedBlock(ByteString infoKey, UnclaimedInfoBlock unclaimedInfoBlock) throws RocksDBException {
        storage.newTransaction(t -> {

            Optional<ByteString> accountUnclaimed = storage.getAccountUnclaimed(t, unclaimedInfoBlock.getAccountId());

            boolean success = false;
            delete:
            {
                if (accountUnclaimed.isEmpty() || NULL_HASH.equals(accountUnclaimed.get())) {
                    LOGGER.warn("Delete info to soon, for account {}!", Base32Helper.encode(unclaimedInfoBlock.getAccountId().toByteArray()));
                    break delete;
                }

                ByteString unclaimedBlockValueKey = accountUnclaimed.get();
                ByteString unclaimedBlockValue = getUnclaimedBlock(t, unclaimedBlockValueKey);

                if (unclaimedBlockValueKey.equals(unclaimedInfoBlock.getBlockHash())) {
                    storage.putAccountUnclaimed(t, unclaimedInfoBlock.getAccountId(), unclaimedBlockValue);
                    success = true;
                    break delete;
                }

                while (!NULL_HASH.equals(unclaimedBlockValue)) {

                    if (unclaimedBlockValue.equals(unclaimedInfoBlock.getBlockHash())) {
                        ByteString tmpUnclaimedBlockValue = getUnclaimedBlock(t, unclaimedBlockValue);
                        storage.putUnclaimedBlock(t, unclaimedBlockValueKey, tmpUnclaimedBlockValue);
                        success = true;
                        break delete;
                    }
                    unclaimedBlockValueKey = unclaimedBlockValue;
                    unclaimedBlockValue = getUnclaimedBlock(t, unclaimedBlockValueKey);
                }
            }

            if (success) {
                storage.deleteUnclaimedBlock(t, unclaimedInfoBlock.getBlockHash());
                storage.deleteUnclaimedInfoBlock(t, infoKey);
            }
        });
    }

    private ByteString getUnclaimedBlock(Transaction t, ByteString blockHash) throws RocksDBException {

        Optional<ByteString> unclaimedBlock = storage.getUnclaimedBlock(t, blockHash);

        if (unclaimedBlock.isEmpty()) {
            throw new IllegalStateException("Should not happen! For blockHash:" + Base32Helper.encode(blockHash.toByteArray()));
        }

        return unclaimedBlock.get();
    }

    private void createUnclaimedBlock(ByteString infoKey, UnclaimedInfoBlock unclaimedInfoBlock) throws RocksDBException {
        storage.newTransaction(t -> {

            Optional<ByteString> accountUnclaimed = storage.getAccountUnclaimed(t, unclaimedInfoBlock.getAccountId());

            if (accountUnclaimed.isEmpty() || NULL_HASH.equals(accountUnclaimed.get())) {
                storage.putUnclaimedBlock(t, unclaimedInfoBlock.getBlockHash(), NULL_HASH);
            } else {
                storage.putUnclaimedBlock(t, unclaimedInfoBlock.getBlockHash(), accountUnclaimed.get());
            }

            storage.putAccountUnclaimed(t, unclaimedInfoBlock.getAccountId(), unclaimedInfoBlock.getBlockHash());

            storage.deleteUnclaimedInfoBlock(t, infoKey);
        });
    }

}
