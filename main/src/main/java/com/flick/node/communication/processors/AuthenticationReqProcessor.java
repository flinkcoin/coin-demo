package com.flick.node.communication.processors;

import com.flick.data.proto.common.Common.Node;
import com.flick.data.proto.common.Common.NodeAddress;
import com.flick.node.communication.*;
import com.flick.data.proto.communication.Message;
import com.flick.node.configuration.Config;
import com.flick.node.managers.CryptoManager;
import com.flick.node.managers.NodeManager;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AuthenticationReqProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationReqProcessor.class);

    private final CryptoManager cryptoManager;
    private final NodeManager nodeManager;

    @Inject
    public AuthenticationReqProcessor(CryptoManager cryptoManager, NodeManager nodeManager) {
        this.cryptoManager = cryptoManager;
        this.nodeManager = nodeManager;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        Message.AuthenticationReq authenticationReq = any.unpack(Message.AuthenticationReq.class);

        Message.AuthenticationRes.Builder authenticationResBuilder = Message.AuthenticationRes.newBuilder();

        ByteString token = authenticationReq.getToken();

        Message.AuthenticationRes.Body.Builder bodyBuilder = Message.AuthenticationRes.Body.newBuilder();

        Node.Builder nodeBuilder = Node.newBuilder();
        nodeBuilder.setNodeId(nodeManager.getNodeId());
        nodeBuilder.setPublicKey(nodeManager.getPublicKey());

        NodeAddress.Builder nodeAddressBuilder = NodeAddress.newBuilder();
        nodeAddressBuilder.setIp(Config.get().ip());
        nodeAddressBuilder.setPort(Config.get().port());

        bodyBuilder.setNode(nodeBuilder);
        bodyBuilder.setNodeAddress(nodeAddressBuilder);
        bodyBuilder.setToken(token);

        Message.AuthenticationRes.Body bodyBuild = bodyBuilder.build();

        authenticationResBuilder.setBody(bodyBuild);

        ByteString signature = cryptoManager.signData(bodyBuild.toByteString());
        authenticationResBuilder.setSignature(signature);

        Message.Builder builder = Message.newBuilder();
        builder.setAny(Any.pack(authenticationResBuilder.build()));
        ctx.write(builder.build());
    }

}
