package org.flinkcoin.node.communication.processors;

import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.data.proto.communication.Message.IAmAlive;
import org.flinkcoin.node.managers.CryptoManager;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.communication.ChannelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IAmAliveProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(IAmAliveProcessor.class);

    private final CryptoManager nodeManager;

    @Inject
    public IAmAliveProcessor(CryptoManager cryptoManager) {
        this.nodeManager = cryptoManager;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        IAmAlive iAmAlive = any.unpack(IAmAlive.class);
    }

}
