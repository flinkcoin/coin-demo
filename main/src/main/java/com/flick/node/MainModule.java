package com.flick.node;

import org.flinkcoin.data.proto.communication.Message;
import com.flick.node.communication.CommonProcessor;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;

public class MainModule extends AbstractModule {

    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 10;

    @Override
    protected void configure() {

    }

    @Provides
    @Named("server")
    public ChannelInitializer<SocketChannel> provideServerChannelInit(final CommonProcessor commonHandler) {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new ProtobufVarint32FrameDecoder());
                p.addLast(new ProtobufDecoder(Message.getDefaultInstance()));

                p.addLast(new ProtobufVarint32LengthFieldPrepender());
                p.addLast(new ProtobufEncoder());

                p.addLast(new IdleStateHandler(READ_TIMEOUT, WRITE_TIMEOUT, 0, TimeUnit.SECONDS));

                p.addLast(commonHandler);
            }
        };
    }

    @Provides
    @Named("client")
    public ChannelInitializer<SocketChannel> provideClientChannelInit(final CommonProcessor commonHandler) {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();

                p.addLast(new ProtobufVarint32FrameDecoder());
                p.addLast(new ProtobufDecoder(Message.getDefaultInstance()));

                p.addLast(new ProtobufVarint32LengthFieldPrepender());
                p.addLast(new ProtobufEncoder());

                p.addLast(new IdleStateHandler(READ_TIMEOUT, WRITE_TIMEOUT, 0, TimeUnit.SECONDS));

                p.addLast(commonHandler);
            }
        };
    }
}
