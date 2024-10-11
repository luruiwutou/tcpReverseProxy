package com.forward.core.netty.pool;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.forward.core.constant.Constants;
import com.forward.core.netty.DataBusConstant;
import com.forward.core.netty.config.NettyClientPoolProperties;
import com.forward.core.netty.handler.NettyChannelPoolHandler;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.utils.NettyUtils;
import com.forward.core.utils.TraceIdThreadLocal;
import com.forward.core.utils.balance.QueueBalance;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Slf4j
public class NettyClientPool {

    /**
     * volatile保持线程之间的可见性，连接池的创建是单例，在这里可加可不加
     */
    volatile private static NettyClientPool nettyClientPool;
    /**
     * key为目标主机的InetSocketAddress对象，value为目标主机对应的连接池
     */
    public ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;

    final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Map<InetSocketAddress, FixedChannelPool> pools = new HashMap<>(4);
    private List<InetSocketAddress> addressList;
    private Map<InetSocketAddress, NettyChannelPoolHandler> poolHandlers = new HashMap<>(4);

    public NettyClientPoolProperties getClientConfig() {
        return clientConfig;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public Map<InetSocketAddress, NettyChannelPoolHandler> getPoolHandlers() {
        return poolHandlers;
    }

    private final Consumer<Channel> customizeHandlerMapCon;
    private final NettyClientPoolProperties clientConfig;

    private QueueBalance<InetSocketAddress> queueBalance = new QueueBalance<>();


    public NettyClientPool(Consumer<Channel> customizeHandlerMapCon, EventLoopGroup workerGroup, EventLoopGroup bossGroup, NettyClientPoolProperties clientConfig) {
        this.clientConfig = clientConfig;
        this.customizeHandlerMapCon = customizeHandlerMapCon;
        this.workerGroup = workerGroup;
        this.bossGroup = bossGroup;
        build();
    }

    /**
     * 单例
     *
     * @return
     */
//    public static NettyClientPool getInstance(Consumer<Channel> customizeHandlerMapCon, EventLoopGroup workerGroup, EventLoopGroup bossGroup, NettyClientPoolProperties clientConfig) {
//        if (nettyClientPool == null) {
//            synchronized (NettyClientPool.class) {
//                if (nettyClientPool == null) {
//                    nettyClientPool = new NettyClientPool(customizeHandlerMapCon, workerGroup, bossGroup, clientConfig);
//                }
//            }
//        }
//        return nettyClientPool;
//    }
    public void build() {
        log.info("NettyClientPool 创建......");
//        putInetAddresses(clientConfig.getServers());
        putInetAddresses(clientConfig.getRemoteServerAndLocalPort().keySet());

        poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress key) {
                Bootstrap strap = new Bootstrap().group(bossGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_KEEPALIVE, true);
                NettyChannelPoolHandler nettyChannelPoolHandler = new NettyChannelPoolHandler(customizeHandlerMapCon);
                poolHandlers.put(key, nettyChannelPoolHandler);
                String port = clientConfig.getRemoteServerAndLocalPort().get(NettyUtils.getAddress(key));
                if (StringUtil.isNotEmpty(port) && !Constants.LOCAL_PORT_RULE_SINGLE.equals(port)) {
                    strap.localAddress(Integer.parseInt(port));
                }
                return new FixedChannelPool(strap.remoteAddress(key), nettyChannelPoolHandler, clientConfig.getPoolSize() / addressList.size());
            }
        };

        for (InetSocketAddress address : addressList) {
            pools.put(address, poolMap.get(address));
        }
        log.info("NettyClientPool 创建完成......");
        log.info("all pools:{}", JSON.toJSONString(pools));
    }

    /**
     * <pre>功能描述:
     * 根据随机数取出随机的server对应pool，从pool中取出channel
     *   pool.acquiredChannelCount(); 对应池中的channel数目
     *   连接池的动态扩容： 指定最大连接数为{poolSize/addressListSize},如果连接池队列中取不到channel，会自动创建channel，默认使用FIFO的获取方式，回收的channel优先被再次get到
     *   SERVER的宕机自动切换: 指定重试次数，get()发生连接异常，则对随机数+1，从下一个池中重新获取,
     *
     *   后期如有必要可优化为：Server注册到注册中心，从注册中心获取连接池对应的address，或者注册到zookeeper中，都需要单独写实现
     *
     * </pre>
     *
     * @param serverAddress
     * @return io.netty.channel.Channel
     */
    public Channel getChannel(String serverAddress) {
        log.info("准备从pool中获取channel");
        return getChannel(serverAddress, 0);
    }

    public Channel getChannel(String serverAddress, long retry) {
        log.info("NettyPool GetChannel 开始");
        Channel channel = null;
        try {
            //按时间戳取余
            InetSocketAddress address = null;
            if (StringUtil.isNotBlank(serverAddress)) {
                Optional<InetSocketAddress> inetSocketAddress = addressList.stream().filter(socket -> serverAddress.equals(socket.toString().split("/")[1])).findAny();
                if (inetSocketAddress.isPresent()) {
                    address = inetSocketAddress.get();
                }
            } else {
                address = queueBalance.chooseOne(addressList);
            }
            synchronized (address) {
                log.info("pool address {}", address);
                FixedChannelPool pool = pools.get(address);
                String traceId = TraceIdThreadLocal.get();
                Future<Channel> future = pool.acquire();
                channel = future.get();
                AttributeKey<InetSocketAddress> randomID = AttributeKey.valueOf(DataBusConstant.RANDOM_KEY);
                //添加TraceId
                Attribute<String> traceIdAttr = channel.attr(AttributeKey.valueOf(Constants.TRACE_ID));
                traceIdAttr.set(traceId);
                channel.attr(randomID).set(address);
            }
            //如果是因为服务端挂点，连接失败而获取不到channel，则随机数执行+1操作，从下一个池获取
        } catch (ExecutionException e) {
            log.info("NettyPool GetChannel 失败 尝试重新获取");
            log.error(e.getMessage());
            if (retry <= clientConfig.getRetryTimes()) {
                return getChannel(serverAddress, ++retry);
            } else if (clientConfig.getRetryTimes() < retry && retry <= clientConfig.getRetryTimes() * 2) {
                return getChannel(serverAddress, ++retry);
            } else {
                log.error("没有可以获取到channel连接的server，server list [{}]", addressList);
                throw new RuntimeException("没有可以获取到channel连接的server");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("获取channel 异常", e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取channel 异常", e);
        }
        log.info("NettyPool GetChannel 结束，channel为 {}", channel);
        return channel;
    }


    /**
     * <pre>功能描述:
     *  回收channel进池，需要保证随机值和getChannel获取到的随机值是同一个，才能从同一个pool中释放资源
     * </pre>
     *
     * @param ch
     * @return void
     */
    public void release(Channel ch) {
        InetSocketAddress address = ch.attr(AttributeKey.<InetSocketAddress>valueOf(DataBusConstant.RANDOM_KEY)).get();
        ch.flush();
        String traceId = TraceIdThreadLocal.get();
        pools.get(address).release(ch).addListener(futureListener -> {
            MDC.put("traceId", traceId);
            log.info("|-->回收Channel. Channel ID:  {}", ch.id());
            MDC.remove("traceId");
        });
    }


    /**
     * <pre>功能描述:
     * 获取服务端server列表，每个server对应一个pool
     * </pre>
     *
     * @param addresses
     * @return void
     */
    public void putInetAddresses(Collection<String> addresses) {
        addressList = new ArrayList<>(4);
        if (CollectionUtil.isEmpty(addresses)) {
            throw new RuntimeException("address列表为空");
        }
        for (String address : addresses) {
            String[] split = address.split(":");
            if (split.length <= 1) {
                throw new RuntimeException("[" + address + "]不符合IP:PORT格式");
            }
            addressList.add(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        }
    }

}