package com.forward.core.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class W {

    public static void main(String[] args) throws InterruptedException {
        new W("localhost:19003").start(19001);

    }

    private final String relayAddress;

    public W(String relayAddress) {
        this.relayAddress = relayAddress;
    }

    public void start(int localPort) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(localPort))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelFuture relayChannelFuture = relayConnect(ch.eventLoop(), relayAddress);
                            ch.pipeline().addLast(new ForwardHandler(relayChannelFuture.channel()));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind().sync();
            log.info("Server started and listen on {}", f.channel().localAddress());
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private ChannelFuture relayConnect(EventLoop eventLoop, String relayAddress) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new RelayHandler(relayAddress, eventLoop));

        return b.connect(InetSocketAddress.createUnresolved(relayAddress.split(":")[0], Integer.parseInt(relayAddress.split(":")[1]))).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("Connect to relay server {} success", relayAddress);
            } else {
                log.warn("Connect to relay server {} failed, reconnect after 5s", relayAddress);
                eventLoop.schedule(() -> relayConnect(eventLoop, relayAddress), 5, TimeUnit.SECONDS);
            }
        });
    }

    private static class ForwardHandler extends ChannelInboundHandlerAdapter {
        private final Channel relayChannel;

        public ForwardHandler(Channel relayChannel) {
            this.relayChannel = relayChannel;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (relayChannel.isActive()) {
                relayChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        log.warn("Relay to remote server failed, close the channel from client {}", ctx.channel().remoteAddress());
                        relayChannel.close();
                    }
                });
            }else{

            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("Client {} offline", ctx.channel().remoteAddress());
            relayChannel.close();
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.warn("Exception caught from client {}", ctx.channel().remoteAddress(), cause);
            relayChannel.close();
            ctx.channel().close();
        }
    }

    private static class RelayHandler extends ChannelInboundHandlerAdapter {
        private final String remoteAddress;
        private final EventLoopGroup group;
        private final Bootstrap bootstrap;
        private Channel relayChannel;
        private static final ChannelGroup clientChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        public RelayHandler(String remoteAddress, EventLoopGroup group) {
            this.remoteAddress = remoteAddress;
            this.group = group;
            this.bootstrap = new Bootstrap();
            this.bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.warn("Remote server {} offline, try to reconnect", remoteAddress);
                                    super.channelInactive(ctx);
                                    reconnect();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    log.warn("Exception caught from remote server {}", remoteAddress, cause);
                                    super.exceptionCaught(ctx, cause);
                                    reconnect();
                                }

                                private void reconnect() {
                                    group.schedule(() -> {
                                        log.info("Try to connect to remote server {}", remoteAddress);
                                        bootstrap.connect(InetSocketAddress.createUnresolved(remoteAddress.split(":")[0],
                                                Integer.parseInt(remoteAddress.split(":")[1]))).addListener((ChannelFutureListener) future -> {
                                            if (future.isSuccess()) {
                                                log.info("Connect to remote server {} success", remoteAddress);
                                                relayChannel = future.channel();
                                                clientChannelGroup.add(relayChannel);
                                            } else {
                                                log.warn("Connect to remote server {} failed, reconnect after 5s", remoteAddress);
                                                reconnect();
                                            }
                                        });
                                    }, 5, TimeUnit.SECONDS);
                                }
                            });
                        }
                    });
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("New client {} connected, try to connect to remote server {}", ctx.channel().remoteAddress(), remoteAddress);
            bootstrap.connect(InetSocketAddress.createUnresolved(remoteAddress.split(":")[0], Integer.parseInt(remoteAddress.split(":")[1]))).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("Connect to remote server {} success", remoteAddress);
                    relayChannel = future.channel();
                } else {
                    log.warn("Connect to remote server {} failed, close the channel from client {}", remoteAddress, ctx.channel().remoteAddress());
                    ctx.channel().close();
                }
            });
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 遍历 clientChannelGroup 中所有的客户端连接
            for (Channel clientChannel : clientChannelGroup) {
                if (clientChannel.isActive()) {
                    // 向客户端发送数据
                    clientChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            log.warn("Relay to remote server failed, close the channel from client {}",clientChannel) ;
                            clientChannelGroup.remove(clientChannel);
                        }
                    });
                } else {
                    log.warn("channel is not active, message dropped.");
                    // 客户端连接已经断开，从 clientChannelGroup 中移除
                    clientChannelGroup.remove(clientChannel);
                }
            }
        }
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("Client {} offline", ctx.channel().remoteAddress());
            if (relayChannel != null) {
                relayChannel.close();
            }
            clientChannelGroup.remove(relayChannel);
//            relayChannel.close();
//            super.channelInactive(ctx);
            log.info("Channel to remote server {} inactive.", relayChannel);

        }
        private static boolean isReconnectEnabled(){
            return true;
        };
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.warn("Exception caught from client {}", ctx.channel().remoteAddress(), cause);
            relayChannel.close();
            ctx.channel().close();
        }
    }

}
