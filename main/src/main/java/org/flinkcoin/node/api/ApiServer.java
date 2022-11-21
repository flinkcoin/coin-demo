package org.flinkcoin.node.api;

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

import org.flinkcoin.node.configuration.Config;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApiServer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiServer.class);

    private final Server server;

    @Inject
    public ApiServer( AccountServiceImpl accountServiceImpl) {
        this.server = ServerBuilder
                .forPort(Config.get().apiPort())
                .addService(accountServiceImpl)
                .build();
    }

    public void init() throws IOException {
        server.start();
    }

    @Override
    public void close() {
        try {
            server.shutdown();
            server.awaitTermination();
        } catch (InterruptedException ex) {
            LOGGER.error("Problem!", ex);
        }
    }

}
