package org.flinkcoin.node.caches;

import org.flinkcoin.data.proto.common.Common.NodeAddress;
import org.flinkcoin.helper.helpers.Base32Helper;
import org.flinkcoin.node.storage.ColumnFamily;
import org.flinkcoin.node.storage.Storage;
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
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeAddressCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeAddressCache.class);

    private static final int MAX_ELEMENTS = 10000;
    private static final int EXPIRY_TIME = 6;

    private final Cache<ByteString, NodeAddress> cache;
    private final Storage storage;

    @Inject
    public NodeAddressCache(Storage storage) {
        this.storage = storage;

        this.cache = new Cache2kBuilder<ByteString, NodeAddress>() {
        }
                .name("NodeAddressCache")
                .eternal(false)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.HOURS)
                .loader(new CacheLoader< ByteString, NodeAddress>() {
                    @Override
                    public NodeAddress load(final ByteString nodeId) throws Exception {
                        return findNode(nodeId);
                    }
                })
                .build();
    }

    private NodeAddress findNode(ByteString nodeId) throws IllegalStateException, RocksDBException, InvalidProtocolBufferException {

        byte[] nodeBytes = storage.get(ColumnFamily.NODE_ADDRESS, nodeId);

        if (nodeBytes == null) {
            throw new CacheLoaderException("Cannot find node for node id: " + Base32Helper.encode(nodeId.toByteArray()));
        }

        return NodeAddress.parseFrom(nodeBytes);
    }

    public Optional<NodeAddress> getNode(ByteString nodeId) {
        NodeAddress node;
        try {
            node = cache.get(nodeId);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(node);
    }

}
