package com.flick.node.communication.processors;

import com.flick.data.proto.common.Common;
import com.flick.node.communication.*;
import com.flick.data.proto.communication.Message;
import com.flick.data.proto.communication.Message.BlockRes;
import com.flick.data.proto.communication.Message.BlockVerifyPub;
import com.flick.helper.helpers.UUIDHelper;
import com.flick.node.managers.CryptoManager;
import com.flick.node.managers.NodeManager;
import com.flick.node.voting.BlockVerifyVoting;
import com.flick.node.voting.stock.BlockVerifyStock;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockResProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockResProcessor.class);

    private final CryptoManager cryptoManager;
    private final BlockVerifyVoting blockVerifyVoting;
    private final BlockVerifyStock blockVerifyStock;
    private final NodeManager nodeManager;

    @Inject
    public BlockResProcessor(CryptoManager cryptoManager, BlockVerifyVoting blockVerifyVoting, BlockVerifyStock blockVerifyStock, NodeManager nodeManager) {
        this.cryptoManager = cryptoManager;
        this.blockVerifyVoting = blockVerifyVoting;
        this.blockVerifyStock = blockVerifyStock;
        this.nodeManager = nodeManager;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        BlockRes blockRes = any.unpack(BlockRes.class);

        List<Common.FullBlock> blocksList = blockRes.getBlocksList();

        if (blocksList.isEmpty()) {
            return;
        }

        Common.FullBlock fullBlock = blocksList.get(blocksList.size() - 1);

        blockVerifyStock.putBlock(fullBlock.getBlock().getBlockHash().getHash(), blocksList);

        BlockVerifyPub.Builder blockVerifyBuilder = BlockVerifyPub.newBuilder();

        BlockVerifyPub.Body.Builder bodyBuilder = BlockVerifyPub.Body.newBuilder();

        bodyBuilder.setBlockHash(fullBlock.getBlock().getBlockHash().getHash());
        bodyBuilder.setMsgId(ByteString.copyFrom(UUIDHelper.asBytes()));
        bodyBuilder.setNodeId(nodeManager.getNodeId());

        BlockVerifyPub.Body body = bodyBuilder.build();

        blockVerifyBuilder.setSignature(cryptoManager.signData(body.toByteString()));
        blockVerifyBuilder.setBody(body);

        ctx.write(makeMessage(Any.pack(blockVerifyBuilder.build())));
    }

}
