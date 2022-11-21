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

import org.flinkcoin.helper.helpers.Base32Helper;
import com.google.protobuf.ByteString;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Nodes {

    T000(Network.TEST, "3JEFYDDBOZGXNIL3Z2B3XSD3QI", "ACZO5F23O7I2PCVF4LEUNHNV2PNP3V5EO6KUX3SAKJEFPQM6O5KXA", "127.0.0.1", 7652);
//    B("RCL4T2NQP5AUTNYOFCKSCK73KU", "ACZO5F23O7I2PCVF4LEUNHNV2PNP3V5EO6KUX3SAKJEFPQM6O5KXA", "127.0.0.1", 7651);

    private final Network network;
    private final String nodeId;
    private final String publicKey;
    private final String ip;
    private final int port;

    private Nodes(Network network, String nodeId, String publicKey, String ip, int port) {
        this.network = network;
        this.nodeId = nodeId;
        this.publicKey = publicKey;
        this.ip = ip;
        this.port = port;
    }

    public static List<Nodes> getTestNodes() {

        List<Nodes> nodes = Arrays.asList(Nodes.values());

        return nodes.stream().filter(x -> x.network == Network.TEST).collect(Collectors.toList());
    }

    public static List<Nodes> getProdNodes() {

        List<Nodes> nodes = Arrays.asList(Nodes.values());

        return nodes.stream().filter(x -> x.network == Network.PROD).collect(Collectors.toList());
    }

    public ByteString getNodeId() {
        return ByteString.copyFrom(Base32Helper.decode(nodeId));
    }

    public ByteString getPublicKey() {
        return ByteString.copyFrom(Base32Helper.decode(publicKey));
    }

    public SocketAddress getSocketAddres() {
        return new InetSocketAddress(ip, port);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
