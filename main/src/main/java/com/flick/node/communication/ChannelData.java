package com.flick.node.communication;

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
