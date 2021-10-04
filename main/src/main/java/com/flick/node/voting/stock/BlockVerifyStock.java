package com.flick.node.voting.stock;

import org.flinkcoin.data.proto.common.Common.FullBlock;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.io.CacheLoaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockVerifyStock {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVerifyStock.class);

    private static final int MAX_ELEMENTS = 10000;
    private static final int EXPIRY_TIME = 6;

    private final Cache<ByteString, List<FullBlock>> cache;

    @Inject
    public BlockVerifyStock() {
        this.cache = new Cache2kBuilder<ByteString, List<FullBlock>>() {
        }
                .name("BlockVerifyStock")
                .eternal(false)
                .sharpExpiry(true)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.SECONDS)
                .build();

    }

    public void putBlock(ByteString blockHash, List<FullBlock> blocks) {
        cache.put(blockHash, blocks);
    }

    public List<FullBlock> getBlock(ByteString blockHash) {
        List<FullBlock> blocks = new ArrayList<>();
        try {
            blocks = cache.get(blockHash);
        } catch (CacheLoaderException ex) {
            LOGGER.error("Problem getting data", ex);
        }

        return blocks;
    }

    public void remove(ByteString blockHash) {
        cache.remove(blockHash);
    }
}
