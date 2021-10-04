package org.flinkcoin.node.caches;

import org.flinkcoin.helper.helpers.Base32Helper;
import org.flinkcoin.helper.helpers.ByteHelper;
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
public class ClaimedBlockCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimedBlockCache.class);

    private static final int MAX_ELEMENTS = 100000;
    private static final int EXPIRY_TIME = 24;

    private final Cache<ByteString, Long> cache;
    private final Storage storage;

    @Inject
    public ClaimedBlockCache(Storage storage) {
        this.storage = storage;

        this.cache = new Cache2kBuilder<ByteString, Long>() {
        }
                .name("ClaimedBlockCache")
                .eternal(false)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.HOURS)
                .loader(new CacheLoader< ByteString, Long>() {
                    @Override
                    public Long load(final ByteString blockHash) throws Exception {
                        return findClaimedBlock(blockHash);
                    }
                })
                .build();
    }

    private Long findClaimedBlock(ByteString blockHash) throws IllegalStateException, RocksDBException, InvalidProtocolBufferException {

        byte[] blockBytes = storage.get(ColumnFamily.CLAIMED_BLOCK, blockHash);

        if (blockBytes == null) {
            throw new CacheLoaderException("Cannot find node for node id: " + Base32Helper.encode(blockHash.toByteArray()));
        }

        return ByteHelper.bytesToLong(blockBytes, 0);
    }

    public Optional<Long> getClaimedBlockTime(ByteString blockHash) {
        Long time;
        try {
            time = cache.get(blockHash);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(time);
    }
}
