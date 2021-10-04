package org.flinkcoin.node.communication;

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

import org.flinkcoin.node.configuration.Config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.logging.LogLevel;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServerInitializer implements AutoCloseable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerInitializer.class);
    
    private EventLoopGroup serverGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture closeFuture;
    
    private final Provider<ChannelInitializer<SocketChannel>> channelInitialer;
    
    @Inject
    public ServerInitializer(@Named("server") Provider<ChannelInitializer<SocketChannel>> channelInitialer) {
        this.channelInitialer = channelInitialer;
    }
    
    public void init() throws InterruptedException {
        
        serverGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        ServerBootstrap bootStrap = new ServerBootstrap();
        bootStrap.group(serverGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(channelInitialer.get());
        
        closeFuture = bootStrap.bind(Config.get().port()).sync().channel().closeFuture();
    }
    
    @Override
    public void close() {
        serverGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        try {
            closeFuture.sync();
        } catch (InterruptedException ex) {
            LOGGER.error("Problem shutting down!", ex);
        }
    }
}
