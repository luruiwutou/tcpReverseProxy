package com.forward.core.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhaoLing
 * <p>
 * 发送HSM的客户端
 * @date
 */
@Slf4j
public class HsmSendClient {

    private final HsmClientProperties hsmClientProperties;

    public HsmSendClient(HsmClientProperties hsmClientProperties) {
        this.hsmClientProperties = hsmClientProperties;
    }

    /**
     * netty client bootstrap
     */
    private static final DefaultEventLoop NETTY_RESPONSE_PROMISE_NOTIFY_EVENT_LOOP = new DefaultEventLoop(null, new NamedThreadFactory("NettyResponsePromiseNotify"));

    public static final AttributeKey<HsmRequestContext> CURRENT_REQ_BOUND_WITH_THE_CHANNEL =
            AttributeKey.valueOf("CURRENT_REQ_BOUND_WITH_THE_CHANNEL");

    /**
     * 异步发送并等待获取结果 默认超时 5S
     *
     * @param message
     * @return
     * @throws InterruptedException
     * @throws
     */
    public byte[] sendAndGet(byte[] message) throws InterruptedException {
        NettyClientPool instance = NettyClientPool.getInstance(hsmClientProperties);
        Channel channel = instance.getChannel(new Random().nextInt());
        byte[] bytes = sendAndGet(message, 2, TimeUnit.SECONDS, channel);
        NettyClientPool.release(channel);
        return bytes;
    }

    /**
     * 同步发送并等待获取结果，并设置超时时间
     *
     * @param message
     * @return
     * @throws InterruptedException
     * @throws
     */
    public byte[] sendAndGet(byte[] message, long timeout, TimeUnit timeUnit) {
        //log.info("--- aaaaaaaaa -->hsm1");
        NettyClientPool instance = NettyClientPool.getInstance(hsmClientProperties);
        Channel channel = instance.getChannel(new Random().nextInt());
        byte[] bytes = sendAndGet(message, timeout, timeUnit, channel);
        NettyClientPool.release(channel);
        return bytes;
    }

    private byte[] sendAndGet(byte[] message, long timeout, TimeUnit timeUnit, Channel channel) {
        //log.info("--- aaaaaaaaa -->hsm2");
        Promise<byte[]> defaultPromise = NETTY_RESPONSE_PROMISE_NOTIFY_EVENT_LOOP.newPromise();
        HsmRequestContext context = new HsmRequestContext(message, defaultPromise);
        channel.attr(CURRENT_REQ_BOUND_WITH_THE_CHANNEL).set(context);
        //log.info("--- aaaaaaaaa -->hsm11");
        ChannelFuture channelFuture = channel.writeAndFlush(message);
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> log.info(Thread.currentThread().getName() + " send complete!"));
        //log.info("--- aaaaaaaaa -->hsm12");
        return get(defaultPromise, timeout, timeUnit);
    }

    /**
     * Sends asynchronously and returns a {@link ChannelFuture}
     *
     * @param message A message to send
     * @return ChannelFuture which will be notified when message is sent
     */
    private ChannelFuture sendAsync(byte[] message, Channel channel) {
        log.info("HSM Send HEX Message---->" + HsmUtils.byteToHex(message));
        return channel.writeAndFlush(message);
    }

    public static <V> V get(Promise<V> future, long timeout, TimeUnit timeUnit) {
        if (!future.isDone()) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            future.addListener(new GenericFutureListener<Future<? super V>>() {
                @Override
                public void operationComplete(Future<? super V> future) throws Exception {
                    log.info("received response,listener is invoked");
                    if (future.isDone()) {
                        // io线程会回调该listener
                        countDownLatch.countDown();
                    }
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
