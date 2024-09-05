package com.forward.core.netty.pool;

import com.forward.core.netty.config.NettyClientPoolProperties;
import com.forward.core.netty.handler.NettyChannelPoolHandler;
import com.forward.core.utils.TraceIdThreadLocal;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CupySendClient<T> {
    /**
     * 线程池线程数量,对应CachedThreadPoolExecutor
     */
    private static final int CORE_POLL_SIZE = 2;
    private static final int MAX_POLL_SIZE = Integer.MAX_VALUE;

    //手动创建线程池
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_POLL_SIZE,
            MAX_POLL_SIZE,
            3,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    /**
     * netty client bootstrap
     */
    private static final DefaultEventLoop NETTY_RESPONSE_PROMISE_NOTIFY_EVENT_LOOP = new DefaultEventLoop(null, threadPool);

    public static final AttributeKey<ClientRequestContext> CURRENT_REQ_BOUND_WITH_THE_CHANNEL =
            AttributeKey.valueOf("CURRENT_REQ_BOUND_WITH_THE_CHANNEL");

    private final NettyChannelPoolHandler nettyChannelPoolHandler;
    private final NettyClientPoolProperties clientConfig;

    public NettyClientPool getNettyClientPool() {
        return NettyClientPool.getInstance(nettyChannelPoolHandler, clientConfig);
    }

    /**
     * netty channel池
     */

    public CupySendClient(NettyChannelPoolHandler nettyChannelPoolHandler, NettyClientPoolProperties clientConfig) {
        this.nettyChannelPoolHandler = nettyChannelPoolHandler;
        this.clientConfig = clientConfig;
    }

    /**
     * 异步发送并等待获取结果 默认超时 5S
     *
     * @param message
     * @throws InterruptedException
     * @throws
     */
    public void send(T message) {
        send(null, message);
    }

    public void send(String serverAddress, T message) {
        TraceIdThreadLocal.set(MDC.get("traceId"));
        log.info("准备开始getChannel");
        Channel channel = getChannel(serverAddress);
        log.info("拿到channel");
        send(message, channel);
        NettyClientPool.release(channel);
    }

    /**
     * 可指定发送的服务端，可以不指定
     * 指定就送连接池中的服务地址，eg:127.0.0.1:9000
     * 不指定就送null
     *
     * @param serverAddress
     * @return
     */
    private Channel getChannel(String serverAddress) {
        NettyClientPool instance = NettyClientPool.getInstance(nettyChannelPoolHandler, clientConfig);
        log.info("准备从pool中获取channel");
        return instance.getChannel(serverAddress);
    }


    private void send(T message, Channel channel) {
        log.info("准备发送报文到目的地");
        log.info("channel local address:{} send msg to remote address:{}.", channel.localAddress(), channel.remoteAddress());
        ChannelFuture channelFuture = channel.writeAndFlush(message);
        log.info("发送报文到目的地 write and Flush结束");
        String traceId = TraceIdThreadLocal.get();
        channelFuture.addListener(future -> {
            MDC.put("traceId", traceId);
            log.info("send complete!");
            MDC.remove("traceId");
        });
    }

    /**
     * 同步发送并等待获取结果，并设置超时时间
     *
     * @param message
     * @return
     * @throws InterruptedException
     * @throws
     */
    public <T, R> R sendAndGet(T message, long timeout, TimeUnit timeUnit) {
        Channel channel = getChannel(null);
        R msg = sendAndGet(message, timeout, timeUnit, channel);
        NettyClientPool.release(channel);
        return msg;
    }

    public <T, R> R sendAndGet(String serverAddress, T message, long timeout, TimeUnit timeUnit) {
        Channel channel = getChannel(serverAddress);
        R msg = sendAndGet(message, timeout, timeUnit, channel);
        NettyClientPool.release(channel);
        return msg;
    }

    private <T, V> V sendAndGet(T message, long timeout, TimeUnit timeUnit, Channel channel)  {
        Promise<V> defaultPromise = NETTY_RESPONSE_PROMISE_NOTIFY_EVENT_LOOP.newPromise();
        ClientRequestContext context = new ClientRequestContext(message, defaultPromise);
        channel.attr(CURRENT_REQ_BOUND_WITH_THE_CHANNEL).set(context);
        ChannelFuture channelFuture = channel.writeAndFlush(message);
        channelFuture.addListener(future -> {
            log.info(Thread.currentThread().getName() + " send complete!");
        });
        return get(defaultPromise, timeout, timeUnit);
    }


    public static <V> V get(Promise<V> future, long timeout, TimeUnit timeUnit) {
        if (!future.isDone()) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            future.addListener(future1 -> {
                log.info("received response,listener is invoked");
                if (future1.isDone()) {
                    // io线程会回调该listener
                    countDownLatch.countDown();
                }
            });

            boolean interrupted = false;
            if (!future.isDone()) {
                try {
                    countDownLatch.await(timeout, timeUnit);
                } catch (InterruptedException e) {
                    log.error("e:{}", e);
                    interrupted = true;
                }

            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        if (future.isSuccess()) {
            return future.getNow();
        }
        log.error("wait result time out ");
        return null;
    }
}