/*
 * Copyright © 2021 Flink Foundation (info@flinkcoin.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flinkcoin.node.managers;

import org.flinkcoin.crypto.KeyGenerator;
import org.flinkcoin.crypto.KeyPair;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.common.Common.Node;
import org.flinkcoin.data.proto.common.Common.NodeAddress;
import org.flinkcoin.helper.helpers.Base32Helper;
import org.flinkcoin.node.caches.NodeAddressCache;
import org.flinkcoin.node.caches.NodeCache;
import org.flinkcoin.node.configuration.Config;
import org.flinkcoin.node.configuration.bootstrap.Nodes;
import org.flinkcoin.node.storage.ColumnFamily;
import org.flinkcoin.node.storage.Storage;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class NodeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);

    private final KeyPair keyPair;
    private final EdDSAParameterSpec spec;

    private final ByteString nodeId;

    private final Storage storage;
    private final NodeCache nodeCache;
    private final NodeAddressCache nodeAddressCache;

    @Inject
    public NodeManager(Storage storage, NodeCache nodeCache, NodeAddressCache nodeAddressCache) throws RocksDBException {
        this.nodeId = ByteString.copyFrom(Base32Helper.decode(Config.get().nodeId()));

        this.storage = storage;
        this.nodeCache = nodeCache;
        this.nodeAddressCache = nodeAddressCache;

        keyPair = KeyGenerator.getKeyPairFromSeed(Base32Helper.decode(Config.get().nodeKey()));
        spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
    }

    public EdDSAParameterSpec getSpec() {
        return spec;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public ByteString getPublicKey() {
        return ByteString.copyFrom(keyPair.getPublicKey().getPublicKey());
    }

    public ByteString getNodeId() {
        return nodeId;
    }

    public Optional<Node> getNode(ByteString nodeId) {

        if (this.nodeId.equals(nodeId)) {
            Node.Builder nodeBuilder = Node.newBuilder();
            nodeBuilder.setNodeId(nodeId);
            nodeBuilder.setPublicKey(getPublicKey());
            return Optional.of(nodeBuilder.build());
        }

        return nodeCache.getNode(nodeId);
    }

    public boolean checkNodeExists(ByteString nodeId) {
        Optional<Common.Node> node = nodeCache.getNode(nodeId);

        return node.isPresent();
    }

    public void initBootstrapNodes(List<Nodes> nodes) throws RocksDBException {
        for (Nodes bn : nodes) {

            /* We skeep ourselves */
            if (Objects.equals(bn.getNodeId(), nodeId)) {
                continue;
            }

            Optional<Common.Node> node = nodeCache.getNode(bn.getNodeId());

            if (node.isEmpty()) {
                addNode(bn.getNodeId(), bn.getPublicKey());
                addNodeAddress(bn.getNodeId(), bn.getIp(), bn.getPort());
            }

        }
    }

    public void addNode(ByteString nodeId, ByteString publicKey) throws RocksDBException {
        Node.Builder nodeBuilder = Node.newBuilder();
        nodeBuilder.setNodeId(nodeId);
        nodeBuilder.setPublicKey(publicKey);

        storage.put(ColumnFamily.NODE, nodeId, nodeBuilder.build().toByteString());
    }

    public void addNodeAddress(ByteString nodeId, String ip, int port) throws RocksDBException {

        NodeAddress.Builder nodeAddressBuilder = NodeAddress.newBuilder();
        nodeAddressBuilder.setIp(ip);
        nodeAddressBuilder.setPort(port);

        storage.put(ColumnFamily.NODE_ADDRESS, nodeId, nodeAddressBuilder.build().toByteString());
    }

}
