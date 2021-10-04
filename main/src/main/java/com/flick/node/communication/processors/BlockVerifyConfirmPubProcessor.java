package com.flick.node.communication.processors;

import com.flick.node.communication.*;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.data.proto.communication.Message.BlockConfirmPub;
import org.flinkcoin.helper.Pair;
import com.flick.node.caches.BlockCache;
import com.flick.node.managers.CryptoManager;
import com.flick.node.voting.BlockVerifyVoting;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockVerifyConfirmPubProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockVerifyConfirmPubProcessor.class);

    private final CryptoManager cryptoManager;
    private final BlockVerifyVoting blockVerifyVoting;
    private final BlockCache blockCache;

    @Inject
    public BlockVerifyConfirmPubProcessor(CryptoManager cryptoManager, BlockVerifyVoting blockVerifyVoting, BlockCache blockCache) {
        this.cryptoManager = cryptoManager;
        this.blockVerifyVoting = blockVerifyVoting;
        this.blockCache = blockCache;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        BlockConfirmPub blockHashConfirmPub = any.unpack(BlockConfirmPub.class);

        BlockConfirmPub.Body body = blockHashConfirmPub.getBody();

        if (!cryptoManager.verifyData(body.getNodeId(), body.toByteString(), blockHashConfirmPub.getSignature())) {
            LOGGER.info("Wrong signtaure!");
            return;
        }

        blockVerifyVoting.newBlockVote(Pair.of(body.getNodeId(), body.getBlockHash()));
    }

}
