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

import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.services.BlockVerifyService;
import org.flinkcoin.node.services.FloodService;
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
