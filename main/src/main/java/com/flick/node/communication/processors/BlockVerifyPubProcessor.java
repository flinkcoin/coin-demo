package com.flick.node.communication.processors;

import com.flick.node.communication.*;
import com.flick.data.proto.communication.Message;
import com.flick.node.managers.CryptoManager;
import com.flick.node.services.BlockVerifyService;
import com.flick.node.services.FloodService;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockVerifyPubProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVerifyPubProcessor.class);

    private final CryptoManager cryptoManager;
    private final FloodService messageService;
    private final BlockVerifyService blockExistService;

    @Inject
    public BlockVerifyPubProcessor(CryptoManager cryptoManager, FloodService messageService, BlockVerifyService blockExistService) {
        this.cryptoManager = cryptoManager;
        this.messageService = messageService;
        this.blockExistService = blockExistService;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        Message.BlockVerifyPub blockHashPub = any.unpack(Message.BlockVerifyPub.class);

        Message.BlockVerifyPub.Body body = blockHashPub.getBody();

        if (!cryptoManager.verifyData(body.getNodeId(), body.toByteString(), blockHashPub.getSignature())) {
            LOGGER.info("Wrong signtaure!");
            return;
        }

        ByteString blockHash = body.getBlockHash();
        blockExistService.process(blockHash);

        messageService.newMessage(req);
    }

}
