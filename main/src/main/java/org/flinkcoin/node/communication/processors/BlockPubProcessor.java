package org.flinkcoin.node.communication.processors;

/*-
 * #%L
 * Flink - Node
 * %%
 * Copyright (C) 2021 Flink Foundation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.flinkcoin.node.handlers.IdHandler;
import org.flinkcoin.data.proto.common.Common;
import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.data.proto.communication.Message.BlockPub;
import org.flinkcoin.helper.Pair;
import org.flinkcoin.helper.helpers.DateHelper;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.services.BlockService;
import org.flinkcoin.node.services.FloodService;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.communication.ChannelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockPubProcessor extends BaseProcessor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockPubProcessor.class);
    
    private final CryptoManager cryptoManager;
    private final BlockService blockService;
    private final FloodService messageService;
    private final IdHandler idHandler;
    
    @Inject
    public BlockPubProcessor(CryptoManager cryptoManager, BlockService blockService, FloodService messageService, IdHandler idHandler) {
        this.cryptoManager = cryptoManager;
        this.blockService = blockService;
        this.messageService = messageService;
        this.idHandler = idHandler;
    }
    
    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        BlockPub blockPub = any.unpack(BlockPub.class);
        
        if (idHandler.checkExists(blockPub.getBody().getMsgId())) {
            return;
        } else {
            idHandler.putId(blockPub.getBody().getMsgId(), DateHelper.dateNow().getTime());
        }
        
        BlockPub.Body body = blockPub.getBody();
        
        if (!cryptoManager.verifyData(body.getNodeId(), body.toByteString(), blockPub.getSignature())) {
            LOGGER.info("Wrong signtaure!");
            return;
        }
        
        Common.Block block = body.getBlock();
        
        blockService.newBlock(Pair.of(channelData.getNodeId(), block));
        messageService.newMessage(req);
    }
}
