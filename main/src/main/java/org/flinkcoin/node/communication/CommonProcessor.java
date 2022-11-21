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
package org.flinkcoin.node.communication;

import org.flinkcoin.data.MessageType;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.node.communication.processors.AuthenticationReqProcessor;
import org.flinkcoin.node.communication.processors.AuthenticationResProcessor;
import org.flinkcoin.node.communication.processors.BlockConfirmPubProcessor;
import org.flinkcoin.node.communication.processors.BlockVerifyPubProcessor;
import org.flinkcoin.node.communication.processors.BlockPubProcessor;
import org.flinkcoin.node.communication.processors.BlockReqProcessor;
import org.flinkcoin.node.communication.processors.BlockResProcessor;
import org.flinkcoin.node.communication.processors.BlockVerifyConfirmPubProcessor;
import org.flinkcoin.node.communication.processors.IAmAliveProcessor;
import org.flinkcoin.node.communication.processors.NodePubProcessor;
import org.flinkcoin.node.communication.processors.PaymentReqProcessor;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
@Singleton
public class CommonProcessor extends SimpleChannelInboundHandler<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonProcessor.class);

    private static final Set<MessageType> VALID_AUTHENTICATION_MESSAGES
            = new HashSet<>(Arrays.asList(MessageType.I_AM_ALIVE, MessageType.AUTHENTICATION_REQ, MessageType.AUTHENTICATION_RES));

    private final Map<ByteString, ChannelId> nodes;
    private final Map<ChannelId, ChannelData> channels;
    private final Map<MessageType, BaseProcessor> handlers;

    @Inject
    public CommonProcessor(IAmAliveProcessor iAmAliveHandler, AuthenticationReqProcessor authenticationReqHandler, AuthenticationResProcessor authenticationResHandler,
            NodePubProcessor nodePubHandler, BlockPubProcessor blockPubHandler, BlockVerifyPubProcessor blockVerifyPubHandler, BlockVerifyConfirmPubProcessor blockVerifyConfirmPubHandler,
            BlockReqProcessor blockReqHandler, BlockResProcessor blockResHandler, BlockConfirmPubProcessor blockConfirmPubHandler,
            PaymentReqProcessor paymentReqProcessor) {

        this.nodes = new ConcurrentHashMap<>();
        this.channels = new ConcurrentHashMap<>();
        this.handlers = new ConcurrentHashMap<>();

        this.handlers.put(MessageType.I_AM_ALIVE, iAmAliveHandler);
        this.handlers.put(MessageType.AUTHENTICATION_REQ, authenticationReqHandler);
        this.handlers.put(MessageType.AUTHENTICATION_RES, authenticationResHandler);
        this.handlers.put(MessageType.NODE_PUB, nodePubHandler);
        this.handlers.put(MessageType.BLOCK_PUB, blockPubHandler);
        this.handlers.put(MessageType.BLOCK_CONFIRM_PUB, blockConfirmPubHandler);
        this.handlers.put(MessageType.BLOCK_VERIFY_PUB, blockVerifyPubHandler);
        this.handlers.put(MessageType.BLOCK_VERIFY_CONFIRM_PUB, blockVerifyConfirmPubHandler);
        this.handlers.put(MessageType.BLOCK_REQ, blockReqHandler);
        this.handlers.put(MessageType.BLOCK_RES, blockResHandler);
        this.handlers.put(MessageType.PAYMENT_REQ, paymentReqProcessor);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("active {}{}", ctx.channel().toString(), ctx.channel().id());
        ChannelData channelData = new ChannelData(ctx.channel());
        channels.put(ctx.channel().id(), channelData);

        sendAuthRequest(ctx, channelData);

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("inactive");
        ChannelData channelData = channels.get(ctx.channel().id());
        if (channelData != null && channelData.getNodeId() != null) {
            nodes.remove(channelData.getNodeId());
        }
        channels.remove(ctx.channel().id());
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("channelRegistered {}", ctx.name());
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("channelUnregistered");
        ctx.fireChannelUnregistered();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message req) throws Exception {

        ChannelData channelData = channels.get(ctx.channel().id());

        if (channelData == null) {
            ctx.close();
            throw new IllegalStateException("ChannelData should be set here!");
        }

        Any any = req.getAny();
        MessageType messageType = MessageType.fromTypeUrl(any.getTypeUrl());

        LOGGER.info("Recived message: {}", messageType);

        if (!channelData.isAuthenticated() && !VALID_AUTHENTICATION_MESSAGES.contains(messageType)) {
            LOGGER.info("Wrong message type, not authenticated, should not happen: {}", messageType);
            ctx.close();
            return;
        }

        BaseProcessor baseHandler = handlers.get(messageType);
        baseHandler.handle(ctx, channelData, req);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            handleIdleStateEvent((IdleStateEvent) evt, ctx);
            return;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info("Exception caught!", cause);
        ctx.close();
    }

    public void send(ByteString nodeId, Message message) {

        ChannelId channelId = nodes.get(nodeId);

        if (channelId == null) {
            return;
        }

        ChannelData channelData = channels.get(channelId);
        if (channelData == null) {
            return;
        }
        Channel channel = channelData.getChannel();
        channel.writeAndFlush(message);

    }

    public void flood(Message message) {
        for (ChannelData channelData : channels.values()) {
            Channel channel = channelData.getChannel();
            try {
                channel.writeAndFlush(message);
            } catch (Exception ex) {
                LOGGER.debug("Error while flooding!", ex);
            }
        }
    }

    public void flood(Any any) {
        Message.Builder messageBuilder = Message.newBuilder();
        messageBuilder.setAny(any);
        flood(messageBuilder.build());
    }

    public synchronized boolean addNode(ByteString nodeId, ChannelId channelId) {
        ChannelId tmpChannelId = nodes.get(nodeId);
        if (tmpChannelId != null && channels.containsKey(tmpChannelId)) {
            return false;
        }
        nodes.put(nodeId, channelId);
        return true;
    }

    public boolean isNodeConnected(ByteString nodeId) {
        return channels.values().stream().filter(x -> Objects.equals(x.getNodeId(), nodeId)).findFirst().isPresent();
    }

    public long numberOfValidNodes() {
        return channels.values().stream().filter(x -> x.isAuthenticated()).count();
    }

    private static Message buildIAmAliveMessage() {

        Message.IAmAlive.Builder iAmAlive = Message.IAmAlive.newBuilder();
        Message.Builder builder = Message.newBuilder();

        builder.setAny(Any.pack(iAmAlive.build()));
        return builder.build();
    }

    private void handleIdleStateEvent(IdleStateEvent idleStateEvent, ChannelHandlerContext ctx) {
        if (idleStateEvent.state() == IdleState.READER_IDLE) {
            ctx.close();
        } else if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
            ctx.writeAndFlush(buildIAmAliveMessage());
        }
    }

    private void sendAuthRequest(ChannelHandlerContext ctx, ChannelData channelData) {

        Message.AuthenticationReq.Builder authenticationReq = Message.AuthenticationReq.newBuilder();

        authenticationReq.setToken(ByteString.copyFrom(channelData.getToken()));

        Message.Builder builder = Message.newBuilder();
        builder.setAny(Any.pack(authenticationReq.build()));
        ctx.writeAndFlush(builder.build());
    }

}
