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
package org.flinkcoin.node.storage;

import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Optional;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.common.Common.FullBlock;
import org.flinkcoin.data.proto.common.Common.Node;
import org.flinkcoin.data.proto.storage.UnclaimedInfoBlock;
import org.flinkcoin.helper.helpers.ByteHelper;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Storage extends StorageBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(Storage.class);
    private static final byte[] NULL = {0x00};
    public static final ByteString NULL_HASH = ByteString.copyFrom(NULL);

    public Storage() throws RocksDBException {
        super();
    }

    public void putAccount(Transaction t, ByteString accountId, ByteString blockHash) throws RocksDBException {
        t.put(getHandle(ColumnFamily.ACCOUNT), accountId.toByteArray(), blockHash.toByteArray());
    }

    public Optional<ByteString> getAccount(Transaction t, ByteString blockHash) throws RocksDBException, InvalidProtocolBufferException {
        byte[] data = t.get(getHandle(ColumnFamily.ACCOUNT), new ReadOptions(), blockHash.toByteArray());

        return data == null ? Optional.empty() : Optional.of(ByteString.copyFrom(data));
    }

    public Optional<ByteString> getAccountUnclaimed(Transaction t, ByteString blockHash) throws RocksDBException, InvalidProtocolBufferException {
        byte[] data = t.get(getHandle(ColumnFamily.ACCOUNT_UNCLAIMED), new ReadOptions(), blockHash.toByteArray());

        return data == null ? Optional.empty() : Optional.of(ByteString.copyFrom(data));
    }

    public void putAccountUnclaimed(Transaction t, ByteString accountId, ByteString blockHash) throws RocksDBException {
        t.put(getHandle(ColumnFamily.ACCOUNT_UNCLAIMED), accountId.toByteArray(), blockHash.toByteArray());
    }

    public void deleteAccountUnclaimed(Transaction t, ByteString accountId) throws RocksDBException {
        t.delete(getHandle(ColumnFamily.ACCOUNT_UNCLAIMED), accountId.toByteArray());
    }

    public Optional<FullBlock> getBlock(Transaction t, ByteString blockHash) throws RocksDBException, InvalidProtocolBufferException {

        byte[] data = t.get(getHandle(ColumnFamily.BLOCK), new ReadOptions(), blockHash.toByteArray());

        if (data == null) {
            return Optional.empty();
        }

        return Optional.of(FullBlock.parseFrom(data));
    }

    public void deleteUnclaimedBlock(Transaction t, ByteString blockHash) throws RocksDBException {
        t.delete(getHandle(ColumnFamily.UNCLAIMED_BLOCK), blockHash.toByteArray());
    }

    public void putUnclaimedBlock(Transaction t, ByteString blockHash, ByteString nextBlockHash) throws RocksDBException {
        t.put(getHandle(ColumnFamily.UNCLAIMED_BLOCK), blockHash.toByteArray(), nextBlockHash.toByteArray());
    }

    public Optional<ByteString> getUnclaimedBlock(Transaction t, ByteString blockHash) throws RocksDBException {
        byte[] data = t.get(getHandle(ColumnFamily.UNCLAIMED_BLOCK), new ReadOptions(), blockHash.toByteArray());

        return data == null ? Optional.empty() : Optional.of(ByteString.copyFrom(data));
    }

    public void deleteUnclaimedInfoBlock(Transaction t, ByteString blockHash) throws RocksDBException {
        t.delete(getHandle(ColumnFamily.UNCLAIMED_INFO_BLOCK), blockHash.toByteArray());
    }

    public void putUnclaimedInfoBlock(Transaction t, ByteString blockHash, UnclaimedInfoBlock unclaimedInfoBlock) throws RocksDBException {
        t.put(getHandle(ColumnFamily.UNCLAIMED_INFO_BLOCK), blockHash.toByteArray(), unclaimedInfoBlock.toByteArray());
    }

    public void putClaimedBlock(Transaction t, ByteString blockHash, Long time) throws RocksDBException {
        t.put(getHandle(ColumnFamily.CLAIMED_BLOCK), blockHash.toByteArray(), ByteHelper.longToBytes(time));
    }

    public void putBlock(Transaction t, ByteString blockHash, FullBlock block) throws RocksDBException {
        t.put(getHandle(ColumnFamily.BLOCK), blockHash.toByteArray(), block.toByteArray());
    }

    public void putBlock(Transaction t, ByteString blockHash, ByteString block) throws RocksDBException {
        t.put(getHandle(ColumnFamily.BLOCK), blockHash.toByteArray(), block.toByteArray());
    }

    public void putNode(Transaction t, ByteString nodeId, Node node) throws RocksDBException {
        t.put(getHandle(ColumnFamily.NODE), nodeId.toByteArray(), node.toByteArray());
    }

    public void putNodeAddress(Transaction t, ByteString nodeId, Common.NodeAddress nodeAddress) throws RocksDBException {
        t.put(getHandle(ColumnFamily.NODE_ADDRESS), nodeId.toByteArray(), nodeAddress.toByteArray());
    }
}
