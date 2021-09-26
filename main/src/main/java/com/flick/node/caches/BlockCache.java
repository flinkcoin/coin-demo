package com.flick.node.caches;

import com.flick.data.proto.common.Common.FullBlock;
import com.flick.helper.helpers.Base32Helper;
import com.flick.node.storage.ColumnFamily;
import com.flick.node.storage.Storage;
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
public class BlockCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockCache.class);

    private static final int MAX_ELEMENTS = 10000;
    private static final int EXPIRY_TIME = 6;

    private final Cache<ByteString, FullBlock> cache;
    private final Storage storage;

    @Inject
    public BlockCache(Storage storage) {
        this.storage = storage;

        this.cache = new Cache2kBuilder<ByteString, FullBlock>() {
        }
                .name("BlockCache")
                .eternal(false)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.HOURS)
                .loader(new CacheLoader< ByteString, FullBlock>() {
                    @Override
                    public FullBlock load(final ByteString nodeId) throws Exception {
                        return findBlock(nodeId);
                    }
                })
                .build();
    }

    private FullBlock findBlock(ByteString blockHash) throws IllegalStateException, RocksDBException, InvalidProtocolBufferException {

        byte[] blockBytes = storage.get(ColumnFamily.BLOCK, blockHash);

        if (blockBytes == null) {
            throw new CacheLoaderException("Cannot find node for node id: " + Base32Helper.encode(blockHash.toByteArray()));
        }

        return FullBlock.parseFrom(blockBytes);
    }

    public Optional<FullBlock> getBlock(ByteString blockHash) {
        FullBlock block;
        try {
            block = cache.get(blockHash);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(block);
    }
}
