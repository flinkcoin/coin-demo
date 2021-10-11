package org.flinkcoin.node.api;

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
