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

import org.flinkcoin.data.proto.communication.Message;
import org.flinkcoin.data.proto.communication.Message.BlockConfirmPub;
import org.flinkcoin.helper.Pair;
import org.flinkcoin.node.caches.BlockCache;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.voting.BlockVoting;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.communication.ChannelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockConfirmPubProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockConfirmPubProcessor.class);

    private final CryptoManager cryptoManager;
    private final BlockVoting blockVoting;
    private final BlockCache blockCache;

    @Inject
    public BlockConfirmPubProcessor(CryptoManager cryptoManager, BlockVoting blockVoting, BlockCache blockCache) {
        this.cryptoManager = cryptoManager;
        this.blockVoting = blockVoting;
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

        blockVoting.newBlockVote(Pair.of(body.getNodeId(), body.getBlockHash()));
    }

}
