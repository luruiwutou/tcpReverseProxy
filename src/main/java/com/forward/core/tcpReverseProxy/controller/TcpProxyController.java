package com.forward.core.tcpReverseProxy.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.forward.core.constant.Constants;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.ReverseProxyServer;
import com.forward.core.tcpReverseProxy.entity.ChannelProxyConfig;
import com.forward.core.tcpReverseProxy.entity.TcpProxyMapping;
import com.forward.core.tcpReverseProxy.enums.ChannelProxyConfigEnum;
import com.forward.core.tcpReverseProxy.handler.ProxyHandler;
import com.forward.core.tcpReverseProxy.mapper.ProxyConfigMapper;
import com.forward.core.tcpReverseProxy.mapper.TcpProxyMappingMapper;
import com.forward.core.tcpReverseProxy.redis.RedisService;
import com.forward.core.tcpReverseProxy.utils.HostUtils;
import com.forward.core.tcpReverseProxy.utils.LockUtils;
import io.netty.channel.Channel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Tag(name = "代理请求")
@RestController
@RequestMapping("/proxy")
@Slf4j
public class TcpProxyController {
    @Autowired
    TcpProxyMappingMapper mappingMapper;
    @Autowired
    ProxyConfigMapper proxyConfigMapper;
    @Autowired
    RedisTemplate<String, Object> redisTemplateObj;
    @Autowired
    RedisService redisService;

    @GetMapping("/query")
    public ResponseEntity query() {
        List<ChannelProxyConfig> channelProxyConfigs = proxyConfigMapper.selectList(new QueryWrapper<ChannelProxyConfig>().likeLeft("conf_key", "Test"));
        return ResponseEntity.ok(channelProxyConfigs);
    }


    private ReverseProxyServer server;
    private String severAddress = "";

    public String getSeverAddress() {
        return StringUtil.isNotBlank(severAddress) ? severAddress : HostUtils.getLocalServerAddress();
    }

    @PostConstruct
    public void init() {
        // 在应用加载完成后执行的初始化方法逻辑
        try {
            log.info("Start Tcp Proxy Server");
            severAddress = HostUtils.getLocalServerAddress();
            server = new ReverseProxyServer();
            start(ChannelProxyConfigEnum.DEFAULT_ENV.getKeyVal(Constants.ASTERISK, proxyConfigMapper));
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Tcp proxy started failed: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        server.shutDown();
    }

    @GetMapping("/start/{env}")
    public void start(@PathVariable String env) {
        start(Constants.ASTERISK, env);
    }

    @GetMapping("/start/{channel}/{env}")
    public synchronized void start(@PathVariable String channel, @PathVariable String env) {
        if (null == server) {
            server = new ReverseProxyServer();
        }
        Map<String, Map<String, List<String[]>>> hosts = getHostsByEmv(channel, env);
        if (CollectionUtil.isEmpty(hosts)) {
            log.info("env has no configuration ,do nothing");
            return;
        }
        for (Map.Entry<String, Map<String, List<String[]>>> stringMapEntry : hosts.entrySet()) {
            server.putChannelHosts(stringMapEntry.getKey(), stringMapEntry.getValue());
            server.start(server, stringMapEntry.getValue());
            ChannelProxyConfigEnum.RUNTIME_ENV.update(stringMapEntry.getKey(), env, proxyConfigMapper);
        }
    }

    @GetMapping("/changeEnv/{env}")
    public void changeEnv(@PathVariable String env) throws Exception {
        changeEnv(Constants.ASTERISK, env);
    }

    @GetMapping("/changeEnv/{channel}/{env}")
    public void changeEnv(@PathVariable String channel, @PathVariable String env) throws Exception {
        Long start = System.currentTimeMillis();
        if (StringUtil.isNotEmpty(env)) {
            env = env.toUpperCase();
        }
        if (StringUtil.isNotEmpty(channel)) {
            channel = channel.toUpperCase();
        }
        log.info("Changing environment");
        try {
            Map<String, Map<String, List<String[]>>> emvHosts = getHostsByEmv(channel, env);
            Map<String, List<String[]>> channelEmvHosts = Constants.ASTERISK.equals(channel) ? emvHosts.values().stream()
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> newValue)) : emvHosts.get(channel);
            Map<String, Map<String, List<String[]>>> hosts = server.getHosts();
            Map<String, List<String[]>> channelNowHosts = Constants.ASTERISK.equals(channel) ? hosts.values().stream()
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> newValue)) : hosts.get(channel);
            Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts = server.getTargetProxyHandlerForHosts();
            if (CollectionUtil.isEmpty(channelEmvHosts) && CollectionUtil.isEmpty(channelNowHosts)) return;
            List<String> needStartServerPort = CollectionUtil.isEmpty(channelNowHosts) ? channelEmvHosts.keySet().stream().collect(Collectors.toList()) : channelEmvHosts.keySet().stream().filter(key -> !channelNowHosts.keySet().contains(key)).collect(Collectors.toList());
            List<String> needStopServerPort = CollectionUtil.isEmpty(channelEmvHosts) ? channelNowHosts.keySet().stream().collect(Collectors.toList()) : channelNowHosts.keySet().stream().filter(key -> !channelEmvHosts.keySet().contains(key)).collect(Collectors.toList());
            Map<String, List<String[]>> needStartServer = channelEmvHosts.entrySet().stream().filter(entry -> needStartServerPort.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            log.info("need start server:{}", JSON.toJSONString(needStartServer));
            log.info("need stop server:{}", JSON.toJSONString(needStopServerPort));
            if (CollectionUtil.isNotEmpty(needStopServerPort)) {
                for (String port : needStopServerPort) {
                    server.closeChannelConnects(port);
                }
            }
            if (CollectionUtil.isNotEmpty(needStartServer)) {
                server.start(server, needStartServer);
            }
            if (Constants.ASTERISK.equals(channel)) {
                //全部替换
                server.setHosts(emvHosts);
            } else {
                //变更渠道
                server.putChannelHosts(channel, channelEmvHosts);
            }
            ChannelProxyConfigEnum.RUNTIME_ENV.update(channel, env, proxyConfigMapper);
            closeNonConfig(channelEmvHosts, targetProxyHandlerForHosts);

        } finally {
            log.info("Changing environment: {} cost {}", env, System.currentTimeMillis() - start);
        }

    }

    /**
     * 注意 每个渠道不能使用同一个端口
     *
     * @param channel
     * @param env
     * @return channe :{ port : [clientPort,targetIP,targetPort]}
     */
    private Map<String, Map<String, List<String[]>>> getHostsByEmv(String channel, String env) {
        String ipAddress = getSeverAddress();
        String tempChannel = channel;
        if (Constants.ASTERISK.equals(channel)) {
            tempChannel = null;
        }
        List<TcpProxyMapping> tcpProxyMappings = mappingMapper.selectMappingWithTargets(tempChannel, env, ipAddress, Constants.MYBATIS_LOCAL_CLIENT_PORT_SPLIT_REGEX);
        Map<String, Map<String, List<String[]>>> channelPortAddress = tcpProxyMappings.stream().collect(Collectors.groupingBy(TcpProxyMapping::getChannel, Collectors.toMap(TcpProxyMapping::getLocalPort, TcpProxyMapping::getTargetConnections)));
        return channelPortAddress;
//        return tcpProxyMappings.stream().collect(Collectors.toMap(TcpProxyMapping::getLocalPort, TcpProxyMapping::getTargetConnections));
    }


    @PostMapping("/addProxyMapping")
    public void addProxyMapping(@RequestBody TcpProxyMapping mapping) {
        int insert = mappingMapper.insert(mapping);
    }

    @PostMapping("/reload/{env}")
    public void reload(@PathVariable String env) throws Exception {
        LockUtils.executeWithLock(getSeverAddress() + Constants.RELOAD_EMN_KEY, 10l, v -> {
            try {
                reloadSupplier(env);
            } catch (Exception e) {
                log.info("Failed to reload,exception: ", e);
            }
        });
    }

    @PostMapping("/reload/{channel}/{env}")
    public void reload(@PathVariable String channel, @PathVariable String env) throws Exception {
        LockUtils.executeWithLock(getSeverAddress() + Constants.RELOAD_EMN_KEY, 10l, v -> {
            try {
                reloadSupplier(channel, env);
            } catch (Exception e) {
                log.info("Failed to reload,exception: ", e);
            }
        });
    }

    private void reloadSupplier(String env) throws Exception {
        reloadSupplier(Constants.ASTERISK, env);
    }

    private void reloadSupplier(String channel, String env) throws Exception {
        if (StringUtil.isNotEmpty(env)) {
            env = env.toUpperCase();
        }
        if (StringUtil.isNotEmpty(channel)) {
            channel = channel.toUpperCase();
        }
        log.info("reloading env: {} start ", env);
        Long start = System.currentTimeMillis();
        if (server == null) {
            log.info("server is null ,start netty server");
            start(env);
        } else {
            if (!Constants.ASTERISK.equals(channel) && env.equals(ChannelProxyConfigEnum.RUNTIME_ENV.getKeyVal(channel, proxyConfigMapper))) {
                Map<String, List<String[]>> hostsByEmv = getHostsByEmv(channel, env).get(channel);
                Map<String, List<String[]>> channelHosts = server.getHosts().get(channel);
                Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts = server.getTargetProxyHandlerForHosts();
                if (CollectionUtil.isEmpty(hostsByEmv)) {
                    if (CollectionUtil.isEmpty(channelHosts)) {
                        log.info("no server port ,ending reload!");
                        return;
                    }
                    for (String port : channelHosts.keySet()) {
                        server.closeChannelConnects(port);
                    }
                    server.getHosts().remove(channel);
                    return;
                }
                log.info("refresh {} config：{}", env, JSON.toJSONString(hostsByEmv));
                //处理新启的服务端
                Map<String, List<String[]>> needStartServer = hostsByEmv.entrySet().stream().filter(entry -> !channelHosts.keySet().contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                List<String> needStopServerPort = channelHosts.keySet().stream().filter(port -> !hostsByEmv.keySet().contains(port)).collect(Collectors.toList());
                //处理关闭的服务端
                for (String port : needStopServerPort) {
                    server.closeChannelConnects(port);
                }
                server.start(server, needStartServer);
                //变更渠道
                server.putChannelHosts(channel, hostsByEmv);
                closeNonConfig(hostsByEmv, targetProxyHandlerForHosts);
            } else {
                changeEnv(channel, env);
            }
        }
        log.info("reloading env: {} cost {}", env, System.currentTimeMillis() - start);
    }

    /**
     * 关闭非配置的目标服务
     *
     * @param hostsByEmv
     * @param targetProxyHandlerForHosts
     * @return
     */
    private static void closeNonConfig(Map<String, List<String[]>> hostsByEmv, Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts) {
        for (Map.Entry<String, List<String[]>> stringListEntry : hostsByEmv.entrySet()) {
            if (CollectionUtil.isEmpty(targetProxyHandlerForHosts.get(stringListEntry.getKey()))) {
                continue;
            }
            targetProxyHandlerForHosts.get(stringListEntry.getKey()).stream().filter(proxyHandler -> !stringListEntry.getValue().stream().anyMatch(array -> array[1].equals(proxyHandler.getTargetServerAddress()))).forEach(handler -> {
                log.info("close non config target address:{}", handler.getTargetServerAddress());
                handler.initiativeReconnected();
            });
        }
    }

    @GetMapping("/stopTarget/{hostPort}/{targetHost}")
    public void stopTarget(@PathVariable String hostPort, @PathVariable String targetHost) throws Exception {
        server.stopTargetServer(hostPort, targetHost);
    }

    @GetMapping("/runtime/env")
    public ResponseEntity runtimeEnv() {
        return ResponseEntity.ok(ChannelProxyConfigEnum.RUNTIME_ENV.getChannelEnv(proxyConfigMapper));
    }
}
