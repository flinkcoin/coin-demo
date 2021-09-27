package com.flick.node.configuration;

import com.flick.node.configuration.bootstrap.Network;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.Sources;

@Sources({
    "file:flink.config"
})
public interface FlinkConfig extends Config {

    @Key("node.network")
    @DefaultValue("TEST")
    Network network();

    @Key("node.ip")
    @DefaultValue("127.0.0.1")
    String ip();

    @Key("node.port")
    @DefaultValue("7651")
    int port();

    @Key("node.api.port")
    @DefaultValue("8081")
    int apiPort();

    @Key("node.logging")
    @DefaultValue("true")
    boolean httpLogging();

    @Key("node.id")
    String nodeId();

    @Key("node.key")
    String nodeKey();

    @Key("node.data")
    @DefaultValue("data")
    String dataPath();

}
