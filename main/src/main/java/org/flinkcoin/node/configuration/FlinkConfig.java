package org.flinkcoin.node.configuration;

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

import org.flinkcoin.node.configuration.bootstrap.Network;
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
