package org.flinkcoin.node.communication.processors;

/*-
 * #%L
 * Flink - Node
 * %%
 * Copyright (C) 2021 - 2022 Flink Foundation
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
import org.flinkcoin.data.proto.communication.Message.NodePub;
import org.flinkcoin.node.managers.CryptoManager;
import org.flinkcoin.node.voting.NodeVoting;
import com.google.protobuf.Any;
import io.netty.channel.ChannelHandlerContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flinkcoin.node.communication.BaseProcessor;
import org.flinkcoin.node.communication.ChannelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NodePubProcessor extends BaseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodePubProcessor.class);

    private final CryptoManager cryptoManager;
    private final NodeVoting nodeVoting;

    @Inject
    public NodePubProcessor(CryptoManager cryptoManager, NodeVoting nodeVoting) {
        this.cryptoManager = cryptoManager;
        this.nodeVoting = nodeVoting;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ChannelData channelData, Message req) throws Exception {
        Any any = req.getAny();
        NodePub nodeConfirmPub = any.unpack(NodePub.class);

        NodePub.Body body = nodeConfirmPub.getBody();

        if (!cryptoManager.verifyData(body.getNodeId(), body.toByteString(), nodeConfirmPub.getSignature())) {
            LOGGER.info("Wrong signtaure!");
            return;
        }
        
        
        

    }

}
