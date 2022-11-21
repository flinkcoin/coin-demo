package org.flinkcoin.node.handlers;

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
public class IdHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdHandler.class);

    private static final int MAX_ELEMENTS = 1000000;
    private static final int EXPIRY_TIME = 15;

    private final Cache<ByteString, Long> cache;

    @Inject
    public IdHandler() {
        this.cache = new Cache2kBuilder<ByteString, Long>() {
        }
                .name("IdHandler")
                .eternal(false)
                .sharpExpiry(true)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.MINUTES)
                .build();

    }

    public void putId(ByteString id, Long time) {
        cache.put(id, time);
    }

    public Optional<Long> getId(ByteString id) {
        Long time;
        try {
            time = cache.get(id);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(time);
    }

    public boolean checkExists(ByteString id) {
        Optional<Long> time = getId(id);

        return time.isPresent();
    }

    public void remove(ByteString id) {
        cache.remove(id);
    }

}
