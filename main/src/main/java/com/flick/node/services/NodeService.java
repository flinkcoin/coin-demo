package com.flick.node.services;

import org.flinkcoin.data.proto.common.Common.Node;
import org.flinkcoin.data.proto.common.Common.NodeAddress;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.helper.Pair;
import org.flinkcoin.helper.helpers.UUIDHelper;
import com.flick.node.caches.NodeCache;
import com.flick.node.communication.CommonProcessor;
import com.flick.node.configuration.ProcessorBase;
import com.flick.node.managers.CryptoManager;
import com.flick.node.managers.NodeManager;
import com.flick.node.voting.NodeVoting;
import com.google.inject.Singleton;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodeService extends ProcessorBase<Pair<Node, NodeAddress>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeService.class);

    private final Provider<CommonProcessor> commonHandler;
    private final NodeManager nodeManager;
    private final CryptoManager cryptoManager;
    private final NodeVoting nodeVoting;

    @Inject
    public NodeService(Provider<CommonProcessor> commonHandler, NodeCache nodeCache, NodeManager nodeManager, CryptoManager cryptoManager, NodeVoting nodeVoting) {
        super(PublishProcessor.create());
        this.nodeVoting = nodeVoting;
        this.nodeManager = nodeManager;
        this.cryptoManager = cryptoManager;
        this.commonHandler = commonHandler;
        this.publishProcessor
                .onBackpressureBuffer(1000, () -> {
                }, BackpressureOverflowStrategy.DROP_LATEST)
                .observeOn(Schedulers.single())
                .subscribe(this);
    }

    public void newNode(Pair<Node, NodeAddress> pair) {
        publishProcessor.onNext(pair);
    }

    @Override
    public void process(Pair<Node, NodeAddress> pair) {
        try {

            Node node = pair.getFirst();
            NodeAddress nodeAddress = pair.getSecond();

            Message.NodePub.Builder nodeConfirmPubBuilder = Message.NodePub.newBuilder();

            Message.NodePub.Body.Builder bodyBuilder = Message.NodePub.Body.newBuilder();

            bodyBuilder.setNodeId(nodeManager.getNodeId());
            bodyBuilder.setMsgId(ByteString.copyFrom(UUIDHelper.asBytes()));
            bodyBuilder.setNode(node);
            bodyBuilder.setNodeAddress(nodeAddress);

            Message.NodePub.Body body = bodyBuilder.build();
            nodeConfirmPubBuilder.setBody(body);
            nodeConfirmPubBuilder.setSignature(cryptoManager.signData(body.toByteString()));

            Message.Builder builder = Message.newBuilder();
            builder.setAny(Any.pack(nodeConfirmPubBuilder.build()));

            Message m = builder.build();
            commonHandler.get().flood(m);

            nodeVoting.newNodeVote(new Pair<ByteString, Pair<Node, NodeAddress>>(nodeManager.getNodeId(), new Pair<Node, NodeAddress>(node, nodeAddress)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException ex) {
            LOGGER.error("Could not sign", ex);
        } catch (Exception ex) {
            LOGGER.error("Could not sign", ex);
        }
    }

}
