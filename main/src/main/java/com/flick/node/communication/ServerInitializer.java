package com.flick.node.communication;

import com.flick.node.configuration.Config;

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
