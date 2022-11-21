package org.flinkcoin.node.voting.stock;

/*-
 * #%L
 * Flink - Node
 * %%
 * Copyright (C) 2021 - 2022 Flink Foundation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.flinkcoin.data.proto.common.Common.Block;
import com.google.protobuf.ByteString;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.io.CacheLoaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockStock {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockStock.class);

    private static final int MAX_ELEMENTS = 10000;
    private static final int EXPIRY_TIME = 6;

    private final Cache<ByteString, Block> cache;

    @Inject
    public BlockStock() {
        this.cache = new Cache2kBuilder<ByteString, Block>() {
        }
                .name("BlockStock")
                .eternal(false)
                .sharpExpiry(true)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.SECONDS)
                .build();

    }

    public void putBlock(ByteString blockHash, Block block) {
        cache.put(blockHash, block);
    }

    public Optional<Block> getBlock(ByteString blockHash) {
        Block block;
        try {
            block = cache.get(blockHash);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(block);
    }

    public void remove(ByteString blockHash) {
        cache.remove(blockHash);
    }

}
