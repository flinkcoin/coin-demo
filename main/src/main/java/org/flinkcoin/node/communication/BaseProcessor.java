package org.flinkcoin.node.communication;

import org.flinkcoin.data.proto.communication.Message;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProcessor.class);

    public abstract void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception;

    protected void eroorCloseChannel(ChannelHandlerContext ctx) {

        ctx.close();
    }

    public static Message makeMessage(Any any) {
        Message.Builder messageBuilder = Message.newBuilder();
        messageBuilder.setAny(any);

        return messageBuilder.build();
    }

}
