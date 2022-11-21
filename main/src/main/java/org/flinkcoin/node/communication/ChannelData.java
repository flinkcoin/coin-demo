package org.flinkcoin.node.communication;

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

import org.flinkcoin.helper.helpers.RandomHelper;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelData {

    private final Channel channel;
    private final byte[] token;
    private final AtomicBoolean authenticated;
    private ByteString nodeId;

    public ChannelData(Channel channel) {
        this.channel = channel;
        this.authenticated = new AtomicBoolean(false);

        byte[] tmp = new byte[16];
        RandomHelper.get().nextBytes(tmp);

        this.token = tmp;
    }

    public void authenticate() {
        authenticated.set(true);
    }

    public boolean isAuthenticated() {
        return authenticated.get();
    }

    public Channel getChannel() {
        return channel;
    }

    public byte[] getToken() {
        return token;
    }

    public void setNodeId(ByteString nodeId) {
        this.nodeId = nodeId;
    }

    public ByteString getNodeId() {
        return nodeId;
    }

}
