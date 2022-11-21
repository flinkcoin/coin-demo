package org.flinkcoin.node.configuration.bootstrap;

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

import org.flinkcoin.node.managers.NodeManager;
import org.flinkcoin.node.storage.Storage;
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
