package com.forward.core.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
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
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyForwardServer {

    private final SocketAddress[] remoteSocket;

    private final EventLoopGroup workerGroup;


    public NettyForwardServer(SocketAddress... remoteSocket) {
        this.remoteSocket = remoteSocket;
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start(int localPort) throws InterruptedException {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(localPort))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new RelayHandler(workerGroup, remoteSocket));
                    }
                });

        ChannelFuture future = bootstrap.bind().sync().await();
        log.info("Server started, listening on port " + future.channel().localAddress());

    }

    public void stop() {
        log.info("Server stopped!");
        workerGroup.shutdownGracefully();
    }


    private static class RelayHandler extends ChannelInboundHandlerAdapter {
        private final EventLoopGroup workerGroup;
        private final SocketAddress[] remoteSockets;
        private static final Random random = new Random();
        private Channel remoteChannel;
        private SocketAddress remoteSocket;
        private static final ChannelGroup clientChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        private static boolean isReconnectEnabled(){
            return true;
        };
        private ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

        public RelayHandler(EventLoopGroup workerGroup, SocketAddress... remoteSockets) {
            this.workerGroup = workerGroup;
            this.remoteSockets = remoteSockets;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 将接收到的消息转发给本地客户端
            log.info("received message from client :{},forward client :{} forward to server:{}", ctx.channel().remoteAddress(), remoteChannel.localAddress(), remoteSocket);
            // 遍历 clientChannelGroup 中所有的客户端连接
            for (Channel clientChannel : clientChannelGroup) {
                if (clientChannel.isActive()) {
                    // 向客户端发送数据
                    clientChannel.writeAndFlush(msg);
                } else {
                    log.warn("channel is not active, message dropped.");
                    // 客户端连接已经断开，从 clientChannelGroup 中移除
                    clientChannelGroup.remove(clientChannel);
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // 当远程客户端连接建立成功时，创建与远程服务器的连接并保存起来
            log.info("远程客户端建立连接：remoteAddress:{},localAddress:{}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            Bootstrap bootstrap = new Bootstrap();
            remoteSocket = remoteSockets[random.nextInt(remoteSockets.length)];
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(remoteSocket)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ForwardHandler(workerGroup, ch, remoteSocket));
                        }
                    });
            ChannelFuture future = bootstrap.connect();
            future.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    remoteChannel = f.channel();
                    clientChannelGroup.add(remoteChannel);
                    log.info("create Forward Client to connect remote server:{}", remoteSocket);
                } else {
                    log.info("Forward Client failed to connect to remote server:{}", remoteSocket);
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            // 当本地客户端连接断开时，关闭与远程服务器的连接并尝试重连
            if (remoteChannel != null) {
                remoteChannel.close();
            }
            log.info("RelayHandler remote server :{} disconnected,localhost:{}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
             clientChannelGroup.remove(remoteChannel);
            log.info("Channel to remote server {} inactive.", remoteSocket);
            if (isReconnectEnabled()) {
                reconnectExecutor.schedule(this::reconnect, 5, TimeUnit.SECONDS);
            }
        }

        private void reconnect() {
            if (!isReconnectEnabled()) {
                return;
            }
            log.info("Try to reconnect to remote server {}...", remoteSocket);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(remoteSocket)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RelayHandler(workerGroup,remoteSockets));
                        }
                    });
            bootstrap.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("Reconnect to remote server {} successfully.", remoteSocket);
                    remoteChannel = future.channel();
                    clientChannelGroup.add(remoteChannel);
                } else {
                    log.info("Failed to reconnect to remote server {}, will try again later.", remoteSocket);
                    reconnectExecutor.schedule(this::reconnect, 5, TimeUnit.SECONDS);
                }
            });
        }

    }

    private static class ForwardHandler extends ChannelInboundHandlerAdapter {
        private final EventLoopGroup group;
        private final Channel inboundChannel;
        private volatile Channel outboundChannel;
        private final SocketAddress remoteSocket;

        public ForwardHandler(EventLoopGroup group, Channel inboundChannel, SocketAddress remoteSocket) {
            this.group = group;
            this.inboundChannel = inboundChannel;
            this.remoteSocket = remoteSocket;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // 当与远程服务器连接成功时，保存 Channel 对象
            log.info("ForwardHandler client:{} connected remote server:{}", ctx.channel().localAddress(), ctx.channel().remoteAddress());
            outboundChannel = ctx.channel();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // 当与远程服务器连接断开时，进行重连
            log.info("ForwardHandler remote server disconnected,try to reconnect");
            // 断开连接时移除对应的 ctx
            log.info("Remote server {} disconnected.", remoteSocket);
            super.channelInactive(ctx);
            ctx.channel().eventLoop().schedule(this::connect, 5, TimeUnit.SECONDS);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 打印接收到的数据
            log.info("ForwardHandler received data from server: {}", msg);
            // 将读取到的数据转发到远程服务器
            if (outboundChannel.isActive()) {
                outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        inboundChannel.close();
                    }
                });
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // 出现异常时关闭连接
            log.info("Exception caught in forward handler:{},remote address:{} ", cause.getMessage(), ctx.channel().remoteAddress());
            ctx.close();
        }

        private void connect() {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(remoteSocket)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ForwardHandler(group, ch, remoteSocket));
                        }
                    });
            b.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    outboundChannel = future.channel();
                } else {
                    // 连接失败后进行重连
                    future.channel().eventLoop().schedule(this::connect, 5, TimeUnit.SECONDS);
                }
            });
        }
    }
}