package org.flinkcoin.node.caches;

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

import org.flinkcoin.data.proto.common.Common.Node;
import org.flinkcoin.helper.helpers.Base32Helper;
import org.flinkcoin.node.storage.ColumnFamily;
import org.flinkcoin.node.storage.Storage;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.io.CacheLoader;
import org.cache2k.io.CacheLoaderException;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class NodeCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeCache.class);

    private static final int MAX_ELEMENTS = 10000;
    private static final int EXPIRY_TIME = 6;

    private final Cache<ByteString, Node> cache;
    private final Storage storage;

    @Inject
    public NodeCache(Storage storage) throws RocksDBException, InvalidProtocolBufferException {
        this.storage = storage;

        this.cache = new Cache2kBuilder<ByteString, Node>() {
        }
                .name("NodeCache")
                .eternal(false)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.HOURS)
                .loader(new CacheLoader< ByteString, Node>() {
                    @Override
                    public Node load(final ByteString nodeId) throws Exception {
                        return findNode(nodeId);
                    }
                })
                .build();
        warmUp();
    }

    public void warmUp() throws RocksDBException, InvalidProtocolBufferException {
        try (RocksIterator itr = storage.getIterator(ColumnFamily.NODE)) {
            itr.seekToFirst();
            while (itr.isValid()) {
                cache.put(ByteString.copyFrom(itr.key()), Node.parseFrom(itr.value()));
                itr.next();
            }
        }
    }

    public Optional<Node> selectRandomNode() {

        if (cache.entries().size() <= 0) {
            return Optional.empty();
        }

        return cache.entries().stream().skip(new Random().nextInt(cache.entries().size())).map(x -> x.getValue()).findFirst();
    }

    private Node findNode(ByteString nodeId) throws IllegalStateException, RocksDBException, InvalidProtocolBufferException {

        byte[] nodeBytes = storage.get(ColumnFamily.NODE, nodeId);

        if (nodeBytes == null) {
            throw new CacheLoaderException("Cannot find node for node id: " + Base32Helper.encode(nodeId.toByteArray()));
        }

        return Node.parseFrom(nodeBytes);
    }

    public Optional<Node> getNode(ByteString nodeId) {
        Node node;
        try {
            node = cache.get(nodeId);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(node);
    }

}
