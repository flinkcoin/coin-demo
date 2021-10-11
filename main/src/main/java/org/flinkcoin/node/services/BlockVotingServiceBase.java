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
package org.flinkcoin.node.services;

import com.google.protobuf.ByteString;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.storage.UnclaimedInfoBlock;
import org.flinkcoin.helper.helpers.DateHelper;
import org.flinkcoin.helper.helpers.UUIDHelper;
import org.flinkcoin.node.configuration.ProcessorBase;
import org.flinkcoin.node.storage.Storage;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BlockVotingServiceBase extends ProcessorBase<ByteString> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVotingServiceBase.class);

    protected final Storage storage;

    public BlockVotingServiceBase(Storage storage) {
        super(PublishProcessor.create());
        this.storage = storage;
    }

    protected UnclaimedInfoBlock prepareInfoBlock(ByteString accountId, UnclaimedInfoBlock.Action action, ByteString blockHash) {
        UnclaimedInfoBlock unclaimedInfoBlock = UnclaimedInfoBlock.newBuilder()
                .setAccountId(accountId)
                .setAction(action)
                .setBlockHash(blockHash)
                .build();

        return unclaimedInfoBlock;
    }

    protected void persistBlock(Common.FullBlock block, Transaction t) throws RocksDBException {

        if (block.getBlock().getBody().getBlockType() == Common.Block.BlockType.RECEIVE) {
            Long time = DateHelper.dateNow().getTime();
            storage.putClaimedBlock(t, block.getBlock().getBody().getReceiveBlockHash(), time);

            storage.putUnclaimedInfoBlock(t, ByteString.copyFrom(UUIDHelper.asBytes()),
                    prepareInfoBlock(block.getBlock().getBody().getAccountId(), UnclaimedInfoBlock.Action.DELETE, block.getBlock().getBody().getReceiveBlockHash()));
        } else if (block.getBlock().getBody().getBlockType() == Common.Block.BlockType.SEND) {
            storage.putUnclaimedInfoBlock(t, ByteString.copyFrom(UUIDHelper.asBytes()),
                    prepareInfoBlock(block.getBlock().getBody().getSendAccountId(), UnclaimedInfoBlock.Action.CREATE, block.getBlock().getBlockHash().getHash()));
        }

        storage.putBlock(t, block.getBlock().getBlockHash().getHash(), block);
        storage.putAccount(t, block.getBlock().getBody().getAccountId(), block.getBlock().getBlockHash().getHash());
    }

}
