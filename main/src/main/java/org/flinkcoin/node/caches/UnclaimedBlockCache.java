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
package org.flinkcoin.node.caches;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.io.CacheLoader;
import org.cache2k.io.CacheLoaderException;
import org.flinkcoin.helper.helpers.Base32Helper;
import org.flinkcoin.node.storage.ColumnFamily;
import org.flinkcoin.node.storage.Storage;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UnclaimedBlockCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnclaimedBlockCache.class);

    private static final int MAX_ELEMENTS = 100000;
    private static final int EXPIRY_TIME = 1;

    private final Cache<ByteString, ByteString> cache;
    private final Storage storage;

    @Inject
    public UnclaimedBlockCache(Storage storage) {
        this.storage = storage;

        this.cache = new Cache2kBuilder<ByteString, ByteString>() {
        }
                .name("UnclaimedBlockCache")
                .eternal(false)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.SECONDS)
                .loader(new CacheLoader< ByteString, ByteString>() {
                    @Override
                    public ByteString load(final ByteString blockHash) throws Exception {
                        return findUnclaimedBlock(blockHash);
                    }
                })
                .build();
    }

    private ByteString findUnclaimedBlock(ByteString blockHash) throws IllegalStateException, RocksDBException, InvalidProtocolBufferException {

        byte[] blockBytes = storage.get(ColumnFamily.UNCLAIMED_BLOCK, blockHash);

        if (blockBytes == null) {
            throw new CacheLoaderException("Cannot find node for node id: " + Base32Helper.encode(blockHash.toByteArray()));
        }

        return ByteString.copyFrom(blockBytes);
    }

    public Optional<ByteString> getLastUnclaimedBlock(ByteString blockHash) {
        ByteString newBlockHash;
        try {
            newBlockHash = cache.get(blockHash);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(newBlockHash);
    }

}
