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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.SocketAddress;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ClientInitializer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientInitializer.class);

    private final Provider<ChannelInitializer<SocketChannel>> channelInitialer;
    private EventLoopGroup group;
    private Bootstrap bootstrap;

    @Inject
    public ClientInitializer(@Named("client") Provider<ChannelInitializer<SocketChannel>> channelInitialer) {
        this.channelInitialer = channelInitialer;
    }

    public void init() throws InterruptedException {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(channelInitialer.get());
    }

    public ChannelFuture newConnection(SocketAddress socketAddress) {
        return bootstrap.connect(socketAddress);
    }

    @Override
    public void close() {
        group.shutdownGracefully();
    }
}
