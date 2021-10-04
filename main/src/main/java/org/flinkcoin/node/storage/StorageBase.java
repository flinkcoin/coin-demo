package org.flinkcoin.node.storage;

import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.helper.ThrowableConsumer;
import org.flinkcoin.helper.ThrowableFunction;
import org.flinkcoin.helper.helpers.ByteHelper;
import org.flinkcoin.node.configuration.Config;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Transaction;
import org.rocksdb.TransactionDB;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StorageBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBase.class);

    protected TransactionDB transactionDB;
    protected Map<String, ColumnFamilyHandle> map;

    public StorageBase() throws RocksDBException {
        RocksDB.loadLibrary();
        init();
        shutdownHook();
    }

    private void init() throws RocksDBException {

        try (final ColumnFamilyOptions cfOptions = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {
            List<ColumnFamilyDescriptor> cfDescriptors = getColumnFamilies(cfOptions);

            try (DBOptions dbOptions = new DBOptions()
                    .setCreateIfMissing(true)
                    .setCreateMissingColumnFamilies(true);
                    TransactionDBOptions transactionDBOptions = new TransactionDBOptions()) {
                List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
                transactionDB = TransactionDB.open(dbOptions, transactionDBOptions, Config.get().dataPath(), cfDescriptors, cfHandles);

                map = cfHandles.stream()
                        .collect(Collectors.toMap((ThrowableFunction<ColumnFamilyHandle, String>) cfh -> ByteHelper.toString(cfh.getName()), cfh -> cfh));

            }
        }
    }

    public TransactionDB getTransactionDB() {
        return transactionDB;
    }

    public void put(ColumnFamily cf, ByteString key, ByteString value) throws RocksDBException {
        transactionDB.put(getHandle(cf), key.toByteArray(), value.toByteArray());
    }

    public byte[] get(ColumnFamily cf, ByteString key) throws RocksDBException {
        return transactionDB.get(getHandle(cf), key.toByteArray());
    }

    public RocksIterator getIterator(ColumnFamily cf) throws RocksDBException {
        return transactionDB.newIterator(getHandle(cf));
    }

    public long count(ColumnFamily cf) throws RocksDBException {
        long count = 0;
        try (RocksIterator itr = getIterator(cf)) {
            itr.seekToFirst();
            while (itr.isValid()) {
                count++;
                itr.next();
            }
        }

        return count;
    }

    public void newTransaction(ThrowableConsumer<Transaction> c) throws RocksDBException {
        boolean transactionSuccess = false;

        try (Transaction t = begin()) {
            try {
                c.accept(t);
                transactionSuccess = true;
            } finally {
                commit(t, transactionSuccess);
            }
        }
    }

    public <T> T newTransaction(ThrowableFunction<Transaction, T> f) throws RocksDBException {
        boolean transactionSuccess = false;
        T apply;

        try (Transaction t = begin()) {
            try {
                apply = f.apply(t);
                transactionSuccess = true;
            } finally {
                commit(t, transactionSuccess);
            }
        }

        return apply;
    }

    private Transaction begin() {
        try (WriteOptions writeOptions = new WriteOptions()) {
            return transactionDB.beginTransaction(writeOptions);
        }
    }

    private void commit(Transaction t, boolean transactionSuccess) throws RocksDBException {
        if (transactionSuccess) {
            t.commit();
        } else {
            t.rollback();
        }
    }

    public ColumnFamilyHandle getHandle(ColumnFamily cf) {
        return map.get(cf.getName());
    }

    private List<ColumnFamilyDescriptor> getColumnFamilies(final ColumnFamilyOptions cfOptions) {
        List<ColumnFamilyDescriptor> descriptors = new ArrayList<>();

        descriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOptions));

        for (ColumnFamily cf : ColumnFamily.values()) {
            descriptors.add(new ColumnFamilyDescriptor(cf.getName().getBytes(), cfOptions));
        }
        return descriptors;
    }

    public void close() {
        LOGGER.info("Closing down database!");
        for (final ColumnFamilyHandle cfh : map.values()) {
            cfh.close();
        }
        transactionDB.close();
    }

    private void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close();
        }));
    }
}
