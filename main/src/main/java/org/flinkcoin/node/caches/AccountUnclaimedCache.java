package org.flinkcoin.node.caches;

import org.flinkcoin.data.proto.common.Common.Block;
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
public class AccountUnclaimedCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountUnclaimedCache.class);

    private static final int MAX_ELEMENTS = 10000;
    private static final int EXPIRY_TIME = 1;

    private final Cache<ByteString, ByteString> cache;
    private final Storage storage;

    @Inject
    public AccountUnclaimedCache(Storage storage) {
        this.storage = storage;

        this.cache = new Cache2kBuilder<ByteString, ByteString>() {
        }
                .name("AccountUnclaimedCache")
                .eternal(false)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.MILLISECONDS)
                .loader(new CacheLoader< ByteString, ByteString>() {
                    @Override
                    public ByteString load(final ByteString accountId) throws Exception {
                        return findBlockHash(accountId);
                    }
                })
                .build();
    }

    private ByteString findBlockHash(ByteString accountId) throws IllegalStateException, RocksDBException, InvalidProtocolBufferException {

        byte[] hashBytes = storage.get(ColumnFamily.ACCOUNT_UNCLAIMED, accountId);

        if (hashBytes == null) {
            throw new CacheLoaderException("Cannot find block hash for account id: " + Base32Helper.encode(accountId.toByteArray()));
        }

        return ByteString.copyFrom(hashBytes);
    }

    public Optional<ByteString> getLastBlockHash(ByteString accountId) {
        ByteString blockHash;
        try {
            blockHash = cache.get(accountId);
        } catch (CacheLoaderException ex) {
            return Optional.empty();
        }

        return Optional.of(blockHash);
    }
}
