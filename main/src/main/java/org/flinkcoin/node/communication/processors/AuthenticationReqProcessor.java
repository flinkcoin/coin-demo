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
package org.flinkcoin.node.communication.processors;

import org.flinkcoin.data.proto.common.Common.Node;
import org.flinkcoin.data.proto.common.Common.NodeAddress;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.node.configuration.Config;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.managers.NodeManager;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.communication.ChannelData;
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
