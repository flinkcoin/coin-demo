package com.flick.node.managers;

import com.flick.data.proto.common.Common;
import com.flick.helper.helpers.Base32Helper;
import com.flick.node.caches.NodeAddressCache;
import com.flick.node.caches.NodeCache;
import com.flick.node.communication.ClientInitializer;
import com.flick.node.communication.CommonProcessor;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private static final short MIN_PEER_CONNECTIONS = 100;
    private static final short MAX_PEER_CONNECTIONS = 250;
    private static final short NUM_PEER_TO_ADD = 10;
    private static final short NUM_PEER_TO_TRY = NUM_PEER_TO_ADD * 3;

    private final ClientInitializer clientInitializer;
    private NodeManager nodeManager;
    private final CommonProcessor commonHandler;

    private final NodeCache nodeCache;
    private final NodeAddressCache nodeAddressCache;

    @Inject
    public ConnectionManager(ClientInitializer clientInitializer, NodeManager nodeManager, CommonProcessor commonHandler, NodeCache nodeCache,
            NodeAddressCache nodeAddressCache) {
        this.clientInitializer = clientInitializer;

        this.nodeManager = nodeManager;
        this.commonHandler = commonHandler;
        this.nodeCache = nodeCache;
        this.nodeAddressCache = nodeAddressCache;
    }

    public void query() {
        LOGGER.debug("Number of valid nodes: {}", commonHandler.numberOfValidNodes());

        if (commonHandler.numberOfValidNodes() > 100) {
            return;
        }

        Set<ByteString> alreadyConnecting = new HashSet<>();

        int addCounter = 0;
        int tryCounter = 0;
        while (addCounter < NUM_PEER_TO_ADD && tryCounter < NUM_PEER_TO_TRY) {
            tryCounter++;
            Optional<Common.Node> node = nodeCache.selectRandomNode();

            if (node.isEmpty()) {
                break;
            }

            if (commonHandler.isNodeConnected(node.get().getNodeId()) || alreadyConnecting.contains(node.get().getNodeId())) {
                continue;
            }

            LOGGER.debug("Making new connection to node {}", Base32Helper.encode(node.get().getNodeId().toByteArray()));

            Optional<Common.NodeAddress> nodeAddress = nodeAddressCache.getNode(node.get().getNodeId());

            if (nodeAddress.isEmpty()) {
                continue;
            }

            addCounter++;
            SocketAddress sa = new InetSocketAddress(nodeAddress.get().getIp(), nodeAddress.get().getPort());
            clientInitializer.newConnection(sa);
            alreadyConnecting.add(node.get().getNodeId());
        }

    }
}
