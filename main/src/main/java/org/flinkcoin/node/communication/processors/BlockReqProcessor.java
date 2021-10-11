package org.flinkcoin.node.communication.processors;

import org.flinkcoin.data.proto.common.Common.FullBlock;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.data.proto.communication.Message.BlockReq;
import org.flinkcoin.data.proto.communication.Message.BlockRes;
import org.flinkcoin.node.caches.BlockCache;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.communication.ChannelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockReqProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockReqProcessor.class);
    private static final int MAX_TRANSFER_BLOCKS = 1000;

    private final BlockCache blockCache;

    @Inject
    public BlockReqProcessor(BlockCache blockCache) {
        this.blockCache = blockCache;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        BlockReq blockReq = any.unpack(BlockReq.class);

        ByteString fromBlockHash = blockReq.getFromBlockHash();

        BlockRes.Builder blockResBuilder = BlockRes.newBuilder();
        blockResBuilder.setAccountId(blockReq.getAccountId());
        blockResBuilder.setFromBlockHash(blockReq.getFromBlockHash());

        Optional<FullBlock> block = blockCache.getBlock(fromBlockHash);

        if (block.isEmpty()) {
            ctx.write(makeMessage(Any.pack(blockResBuilder.build())));
            return;
        }

        int count = 0;
        blockResBuilder.setBlocks(count, block.get());

        while (block.isPresent() && count < MAX_TRANSFER_BLOCKS) {
            count++;
            ByteString nextBlockHash = block.get().getNext();

            block = blockCache.getBlock(nextBlockHash);

            if (block.isPresent()) {
                blockResBuilder.setBlocks(count, block.get());
            }
        }

        ctx.write(makeMessage(Any.pack(blockResBuilder.build())));
    }

}
