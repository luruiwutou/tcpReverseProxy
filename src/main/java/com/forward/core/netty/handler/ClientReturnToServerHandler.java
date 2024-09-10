package com.forward.core.netty.handler;

import com.forward.core.constant.Constants;
import com.forward.core.netty.listener.ReconnectListener;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.utils.SnowFlake;
import com.forward.core.utils.NettyUtils;
import com.forward.core.utils.balance.QueueBalance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class ClientReturnToServerHandler extends ChannelInboundHandlerAdapter {

    private Supplier<ChannelGroup> clientChannelsSupplier;

    private EventExecutorGroup executorGroup;
    private ReconnectListener reconnectListener;


    public ClientReturnToServerHandler() {
    }

    public ClientReturnToServerHandler(Supplier<ChannelGroup> clientChannelsSupplier, EventExecutorGroup executorGroup, ReconnectListener reconnectListener) {
        this.clientChannelsSupplier = clientChannelsSupplier;
        this.executorGroup = executorGroup;
        this.reconnectListener = reconnectListener;
    }

    QueueBalance<Channel> queueBalance = new QueueBalance<>();

//    RedisService getRedisService() {
//        return SingletonBeanFactory.getSpringBeanInstance(RedisService.class).getSingleton();
//    }
//
//    private void readMsgCache(String hostStr, Channel channel) {
//        try {
//            Long listSize = getRedisService().getListSize(hostStr);
//            if (null == listSize || listSize == 0) return;
//            LockUtils.executeWithLock(getClientChannels().stream().findAny().get().id().asLongText(), (v) -> {
//                log.info("read msg cache from redis ,send to {}", getTargetServerAddress());
//                getRedisService().readMsgCache(hostStr, getReadConsumer(channel));
//            });
//        } catch (Exception e) {
//            log.error("read cache error：", e);
//        }
//    }

    private Channel thisChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //添加重连机制
        ctx.channel().closeFuture().addListener(reconnectListener);
        //当与转发目标服务器进行连接的时候，把当前附表的channel赋值给targetChannel
        thisChannel = ctx.channel();
        if (getClientChannels().isEmpty()) {
            reconnectListener.setReconnect(false);
            log.info("No proxy client channels");
            return;
        }
//        getClientChannels().forEach(ch -> readMsgCache(NettyUtils.getRemoteAddress(ch), ctx.channel()));
        MDC.remove(Constants.TRACE_ID);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 转发消息到客户端
        // 将耗时操作委托给 executorGroup 处理
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        if (getClientChannels().isEmpty()) {
            reconnectListener.setReconnect(false);
            ctx.flush();
            ctx.close();
            return;
        }
        sendByClientChannels(msg);
        MDC.remove(Constants.TRACE_ID);
        ctx.channel().attr(Constants.TRACE_ID_KEY).set(null);
    }

    public void shutdown() {
        getClientChannels().close().addListener((ChannelGroupFutureListener) future -> {
            thisChannel.close();
        });
    }


    private void sendByClientChannels(Object msg) {
        List<Channel> collect = getClientChannels().stream().filter(Channel::isActive).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            log.info("channelGroup {} is not active", getClientChannels().name());
            shutdown();
            return;
        }
//                                if (getRedisService().isRedisConnected()) {
//                                    String longText = getClientChannels().stream().findAny().get().id().asLongText();
//                                    log.info("write msg to redis ,channel id :{} ", longText);
//                                    getRedisService().writeMsgCache(longText, msg);
//                                }
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            if (buf.readableBytes() == 4) {
                log.info("receive from {} write msg: {} to every client", getTargetServerAddress(), ByteBufUtil.hexDump(buf));
                executorGroup.submit(() -> {
                    getClientChannels().writeAndFlush(msg);
                });
                return;
            }
        }
//                                int randomIndex = random.nextInt(collect.size());
        Channel randomChannel = queueBalance.chooseOne(collect);
        if (null == randomChannel) {
            log.info("no random channel,queueBalance:{}", queueBalance);
            return;
        }
        log.info("balance to client :{}", randomChannel);
        String clientAddress = NettyUtils.getRemoteAddress(randomChannel);
        log.info("client channel {} is writeable, {}", clientAddress, randomChannel);
        String traceId = MDC.get(Constants.TRACE_ID);
//                                executorGroup.submit(() -> {
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            MDC.put(Constants.TRACE_ID, traceId);
        }                                    // 转发消息到客户端
        if (msg instanceof ByteBuf) {
            log.info("received from target server {},return to {},Hex msg:{}", getTargetServerAddress(), clientAddress, ByteBufUtil.hexDump((ByteBuf) msg));
            ByteBuf byteBuf = (ByteBuf) msg;
            byteBuf.retain(); // 保持引用计数，因为即将在异步操作中使用它
            log.info("client channel {} is active", clientAddress);
        }
        writeIfWritable(randomChannel, msg);
//                                });
    }

    String getTargetServerAddress() {
        return NettyUtils.getRemoteAddress(thisChannel);
    }

    private void writeIfWritable(Channel channel, Object msg) {
        if (channel.isWritable()) {
            try {
                channel.writeAndFlush(msg).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.warn("Write failed", future.cause());
                    }
                    ReferenceCountUtil.release(msg); // 确保在任务结束后释放 ByteBuf
                });
            } catch (Exception e) {
                if (msg instanceof ByteBuf) {
                    ReferenceCountUtil.release(msg); // 确保在任务结束后释放 ByteBuf
                }
                throw e;
            }
        } else {
            // 处理写入缓冲区仍然不可写的情况，可以选择重试或者其他处理方式
            log.info("Channel is still not writable: {}", channel);
            retryWrite(channel, msg);
        }
    }

    private void retryWrite(Channel channel, Object msg) {
        // 使用 ScheduledExecutorService 实现重试逻辑，这里示意每隔一段时间重试一次
        executorGroup.schedule(() -> {
            writeIfWritable(channel, msg);
        }, 1, TimeUnit.SECONDS); // 每隔1秒重试一次，可以根据实际需求调整
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (StringUtil.isEmpty(MDC.get(Constants.TRACE_ID))) {
            String traceId = SnowFlake.getTraceId();
            MDC.put(Constants.TRACE_ID, traceId);
        }
        cause.printStackTrace();
        ctx.close();
        log.info("目标异常：{}，异常信息：{}，尝试重连！", NettyUtils.getRemoteAddress(ctx.channel()), cause);
        MDC.remove(Constants.TRACE_ID);
    }

    private Consumer getReadConsumer(Channel channel) {
        return msg -> {
            if (msg instanceof ByteBuf) {
                ((ByteBuf) msg).retain();
                log.info("{} send to target :{},Hex msg:{}", NettyUtils.getRemoteAddress(channel), getTargetServerAddress(), ByteBufUtil.hexDump((ByteBuf) msg));
            }
            executorGroup.submit(() -> {
                try {
                    channel.writeAndFlush(msg).addListener(future -> {
                        if (!future.isSuccess()) {
                            log.warn("Write failed", future.cause());
                        }
                        if (msg instanceof ByteBuf) {
                            ReferenceCountUtil.release(msg); // 确保在任务结束后释放 ByteBuf
                        }
                    });
                } catch (Exception e) {
                    if (msg instanceof ByteBuf) {
                        ReferenceCountUtil.release(msg); // 确保在任务结束后释放 ByteBuf
                    }
                    throw e;
                }
            });
        };
    }

    public ChannelGroup getClientChannels() {
        return clientChannelsSupplier.get();
    }
}