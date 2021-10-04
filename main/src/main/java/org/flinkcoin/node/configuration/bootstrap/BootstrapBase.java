package org.flinkcoin.node.configuration.bootstrap;

import org.flinkcoin.node.configuration.Config;
import com.google.protobuf.ByteString;
import java.util.List;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BootstrapBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapBase.class);

    protected ByteString getGenesisAccountId() {
        return Config.get().network().getGenesisAccountId();
    }

    protected ByteString getGenesisBlockHash() {
        return Config.get().network().getGenesisBlockHash();
    }

    protected ByteString getGenesisBlock() {
        return Config.get().network().getGenesisBlock();
    }

    protected List<Nodes> getNodes() {
        return Config.get().network() == Network.PROD ? Nodes.getProdNodes() : Nodes.getTestNodes();
    }

}
