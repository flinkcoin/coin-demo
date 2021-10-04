package com.flick.node.voting;

import com.flick.data.proto.common.Common.Node;
import com.flick.data.proto.common.Common.NodeAddress;
import org.flinkcoin.helper.Pair;
import com.flick.node.configuration.ProcessorBase;
import com.flick.node.storage.Storage;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import inet.ipaddr.HostName;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.rocksdb.RocksDBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeVoting extends ProcessorBase<Pair<ByteString, Pair<Node, NodeAddress>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeVoting.class);

    private static final int MAX_ELEMENTS = 10000;
    private static final int EXPIRY_TIME = 6;

    private static final long WEIGHT_TRESHOLD = 90;

    private final Cache<ByteString, NodeVotes> cache;
    private final Storage storage;

    @Inject
    public NodeVoting(Storage storage) {
        super(PublishProcessor.create());

        this.storage = storage;

        this.publishProcessor
                .onBackpressureBuffer(1000, () -> {
                }, BackpressureOverflowStrategy.DROP_LATEST)
                .observeOn(Schedulers.single())
                .subscribe(this);

        this.cache = new Cache2kBuilder<ByteString, NodeVotes>() {
        }
                .name("NodeVoting")
                .eternal(false)
                .sharpExpiry(true)
                .entryCapacity(MAX_ELEMENTS)
                .expireAfterWrite(EXPIRY_TIME, TimeUnit.SECONDS)
                .build();

    }

    public void newNodeVote(Pair<ByteString, Pair<Node, NodeAddress>> pair) {
        publishProcessor.onNext(pair);
    }

    @Override
    public void process(Pair<ByteString, Pair<Node, NodeAddress>> pair) {
        ByteString nodeId = pair.getFirst();
        Pair<Node, NodeAddress> tmp = pair.getSecond();
        Node node = tmp.getFirst();
        NodeAddress nodeAddress = tmp.getSecond();

        NodeVotes nodeVotes = cache.get(node.getNodeId());

        if (nodeVotes == null) {
            nodeVotes = new NodeVotes();
            cache.put(node.getNodeId(), nodeVotes);
        }

        InetSocketAddress socketAddress = new InetSocketAddress(nodeAddress.getIp(), nodeAddress.getPort());

        boolean success = nodeVotes.addVote(node.getPublicKey(), nodeId, new NodeVote(99, socketAddress.toString().replace("/", "")));

        if (!success) {
            return;
        }

        Optional<String> address = nodeVotes.getBestAddress(node.getPublicKey());
        if (address.isEmpty()) {
            LOGGER.error("Strange!");
            return;
        }

        HostName hostName = new HostName(address.get());
        NodeAddress.Builder nodeAddressBuilder = NodeAddress.newBuilder();
        nodeAddressBuilder.setIp(hostName.getHost());
        nodeAddressBuilder.setPort(hostName.getPort());

        try {
            storage.newTransaction(t -> {
                storage.putNode(t, node.getNodeId(), node);
                storage.putNodeAddress(t, node.getNodeId(), nodeAddressBuilder.build());
            });
            cache.remove(node.getNodeId());
        } catch (RocksDBException ex) {
            LOGGER.error("Could not write vote result to DB", ex);
        }
    }

    public static class NodeVotes {

        private final Map<ByteString, NodeVotesByKey> votesByKey;

        public NodeVotes() {
            votesByKey = new HashMap<>();
        }

        public boolean addVote(ByteString publicKey, ByteString nodeId, NodeVote nodeVote) {

            NodeVotesByKey nodeVotesForKey = votesByKey.get(publicKey);

            if (nodeVotesForKey == null) {
                nodeVotesForKey = new NodeVotesByKey();
                votesByKey.put(publicKey, nodeVotesForKey);
            }

            return nodeVotesForKey.addVote(nodeId, nodeVote);
        }

        public Optional<String> getBestAddress(ByteString publicKey) {

            NodeVotesByKey nodeVotesForKey = votesByKey.get(publicKey);
            if (nodeVotesForKey == null) {
                return Optional.empty();
            }

            return nodeVotesForKey.voteByNode.values().stream().collect(Collectors.groupingBy(x -> x.address,
                    Collectors.summingLong(x -> x.weight))).entrySet().stream().
                    max((e1, e2) -> e1.getValue()
                    .compareTo(e2.getValue()))
                    .map(x -> x.getKey());
        }

    }

    public static class NodeVotesByKey {

        private final Map<ByteString, NodeVote> voteByNode;
        private long sumWeight;

        public NodeVotesByKey() {
            sumWeight = 0;
            voteByNode = new HashMap<>();
        }

        public boolean addVote(ByteString nodeId, NodeVote nodeVote) {

            if (voteByNode.containsKey(nodeId)) {
                LOGGER.debug("Node {} trying to vote multiple times!", nodeId);
                return false;
            }
            voteByNode.put(nodeId, nodeVote);
            sumWeight += nodeVote.weight;

            return sumWeight > WEIGHT_TRESHOLD;
        }

    }

    public static class NodeVote {

        private final long weight;
        private final String address;

        public NodeVote(long weight, String address) {
            this.weight = weight;
            this.address = address;
        }
    }
}
