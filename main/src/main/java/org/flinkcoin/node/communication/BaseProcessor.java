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
