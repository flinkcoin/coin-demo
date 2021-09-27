package com.flick.node.configuration.bootstrap;

import com.flick.node.managers.NodeManager;
import com.flick.node.storage.Storage;
import com.google.protobuf.ByteString;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Bootstrap extends BootstrapBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static final long MAX_SUPPLY = 9000000000000000000L;
    public static final long BASE_UNIT = 10000000000L;

    private final Storage storage;
    private final NodeManager nodeManager;

    @Inject
    public Bootstrap(Storage storage, NodeManager nodeManager) {
        this.storage = storage;
        this.nodeManager = nodeManager;
    }

    public void init() throws RocksDBException {
        nodeManager.initBootstrapNodes(getNodes());
        checkGenesisBlock();
    }

    public void checkGenesisBlock() throws RocksDBException {
        storage.newTransaction(t -> {
            Optional<ByteString> accountBlockHash = storage.getAccount(t, getGenesisAccountId());
            if (accountBlockHash.isPresent()) {
                return;
            }

            storage.putBlock(t, getGenesisBlockHash(), getGenesisBlock());
            storage.putAccount(t, getGenesisAccountId(), getGenesisBlockHash());
        });
    }

}
