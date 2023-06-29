package com.forward.core.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class TcpProxyServer {
    private final int localPort;
    private final String remoteHost;
    private final int remotePort;

    public TcpProxyServer(int localPort, String remoteHost, int remotePort) {
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new TcpProxyFrontendHandler(remoteHost, remotePort));
                        }
                    });

            ChannelFuture f = b.bind(localPort).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        TcpProxyServer server = new TcpProxyServer(16666, "localhost", 20001);
        server.run();
    }

    public class TcpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

        private final String remoteHost;
        private final int remotePort;
        private volatile Channel backendChannel;

        public TcpProxyFrontendHandler(String remoteHost, int remotePort) {
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final Channel inboundChannel = ctx.channel();

            // Start the connection attempt
            Bootstrap b = new Bootstrap();
            b.group(inboundChannel.eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new TcpProxyBackendHandler(inboundChannel))
                    .option(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            // 设置空闲超时时间为 60 秒
            int readerIdleTimeSeconds = 60;
            int writerIdleTimeSeconds = 60;
            int allIdleTimeSeconds = 0;
            ctx.pipeline().addLast(new IdleStateHandler(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds));

// 添加一个监听器，当连接被关闭时打印日志
            ctx.pipeline().addLast(new ChannelDuplexHandler() {
                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    System.out.println("Connection closed: " + ctx.channel().remoteAddress());
                    super.channelInactive(ctx);
                }
            });
            ChannelFuture f = b.connect(remoteHost, remotePort);
            backendChannel = f.channel();
            f.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            });
        }

        @Override
        public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
            if (backendChannel.isActive()) {
                backendChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                });
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (backendChannel != null) {
                closeOnFlush(backendChannel);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            if (ctx.channel().isActive()) {
                ctx.channel().close();
            }
        }

        /**
         * Closes the specified channel after all queued write requests are flushed.
         */
        private void closeOnFlush(Channel ch) {
            if (ch.isActive()) {
                ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    public class TcpProxyBackendHandler extends ChannelInboundHandlerAdapter {
        private final Channel inboundChannel;
        private Channel outboundChannel;

        public TcpProxyBackendHandler(Channel inboundChannel) {
            this.inboundChannel = inboundChannel;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            final Channel backendChannel = ctx.channel();
            outboundChannel = backendChannel;

            backendChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            future.channel().close();
                        }
                    });
                }

                @Override
                public void channelInactive(ChannelHandlerContext ctx) {
                    close();
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    cause.printStackTrace();
                    close();
                }

                private void close() {
                    outboundChannel.close();
                    inboundChannel.close();
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            EventLoop loop = ctx.channel().eventLoop();
            loop.schedule(this::reconnect, 10L, TimeUnit.SECONDS);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.channel().close();
        }

        private void reconnect() {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(outboundChannel.eventLoop())
                    .channel(outboundChannel.getClass())
                    .handler(new TcpProxyBackendHandler(inboundChannel))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.connect(remoteHost, remotePort).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    outboundChannel = future.channel();
                } else {
                    EventLoop loop = outboundChannel.eventLoop();
                    loop.schedule(this::reconnect, 10L, TimeUnit.SECONDS);
                }
            });
        }
    }

}
