package com.forward.core.tcpReverseProxy.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.forward.core.sftp.utils.StringUtil;
import com.forward.core.tcpReverseProxy.ReverseProxyServer;
import com.forward.core.tcpReverseProxy.constant.Constants;
import com.forward.core.tcpReverseProxy.entity.ProxyConfig;
import com.forward.core.tcpReverseProxy.entity.TcpProxyMapping;
import com.forward.core.tcpReverseProxy.enums.ProxyConfigEnum;
import com.forward.core.tcpReverseProxy.handler.ProxyHandler;
import com.forward.core.tcpReverseProxy.mapper.ProxyConfigMapper;
import com.forward.core.tcpReverseProxy.mapper.TcpProxyMappingMapper;
import com.forward.core.tcpReverseProxy.redis.RedisService;
import com.forward.core.tcpReverseProxy.utils.LockUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

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
        List<ProxyConfig> proxyConfigs = proxyConfigMapper.selectList(new QueryWrapper<ProxyConfig>().likeLeft("conf_key", "Test"));
        return ResponseEntity.ok(proxyConfigs);
    }

    @GetMapping("/add")
    public ResponseEntity add() {
        int insert = proxyConfigMapper.insert(new ProxyConfig("Test" + new Random().nextInt(), "" + new Random().nextInt()));
        return ResponseEntity.ok(insert);
    }


    private ReverseProxyServer server;
    private String severAddress = "";

    public String getSeverAddress() {
        return StringUtil.isNotBlank(severAddress)?severAddress:getLocalServerAddress();
    }

    @PostConstruct
    public void init() {
        // 在应用加载完成后执行的初始化方法逻辑
        try {
            log.info("Start Tcp Proxy Server");
            severAddress = getLocalServerAddress();
            start(ProxyConfigEnum.DEFAULT_ENV.getKeyVal(proxyConfigMapper));
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
    public void start(@PathVariable String env) throws Exception {
        Map<String, List<String[]>> hosts = getHostsByEmv(env);
        if (CollectionUtil.isEmpty(hosts)) {
            log.info("env has no configuration ,do nothing");
            return;
        }
        server = ReverseProxyServer.start(hosts);
        ProxyConfigEnum.RUNTIME_ENV.update(env, proxyConfigMapper);
//        reverseProxyServer.bootstrap()
    }

    @GetMapping("/changeEnv/{env}")
    public void changeEnv(@PathVariable String env) throws Exception {
        log.info("Changing environment");
        Long start = System.currentTimeMillis();
        try {
            Map<String, List<String[]>> emvHosts = getHostsByEmv(env);
            Map<String, List<String[]>> hosts = server.getHosts();
            Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts = server.getTargetProxyHandlerForHosts();
            if (CollectionUtil.isEmpty(emvHosts) && CollectionUtil.isEmpty(hosts)) return;
            List<String> needStartServerPort = CollectionUtil.isEmpty(hosts) ? emvHosts.keySet().stream().collect(Collectors.toList()) : emvHosts.keySet().stream().filter(key -> !hosts.keySet().contains(key)).collect(Collectors.toList());
            List<String> needStopServerPort = CollectionUtil.isEmpty(emvHosts) ? hosts.keySet().stream().collect(Collectors.toList()) : hosts.keySet().stream().filter(key -> !emvHosts.keySet().contains(key)).collect(Collectors.toList());
            Map<String, List<String[]>> needStartServer = emvHosts.entrySet().stream().filter(entry -> needStartServerPort.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (CollectionUtil.isNotEmpty(needStopServerPort)) {
                for (String port : needStopServerPort) {
                    server.closeChannelConnects(port);
                }
                server.setServerChannels(new ConcurrentHashMap<>());
            }
            if (CollectionUtil.isNotEmpty(needStartServer)) {
                server.start(server, needStartServer);
            }
            server.setHosts(emvHosts);
            ProxyConfigEnum.RUNTIME_ENV.update(env, proxyConfigMapper);
            closeNonConfig(emvHosts, targetProxyHandlerForHosts);

        } finally {
            log.info("Changing environment: {} cost {}", env, System.currentTimeMillis() - start);
        }

    }


    private Map<String, List<String[]>> getHostsByEmv(@PathVariable String env) throws Exception {
        String ipAddress = getSeverAddress();
        List<TcpProxyMapping> tcpProxyMappings = mappingMapper.selectMappingWithTargets(env, ipAddress, Constants.MYBATIS_LOCAL_CLIENT_PORT_SPLIT_REGEX);
        return tcpProxyMappings.stream().collect(Collectors.toMap(TcpProxyMapping::getLocalPort, TcpProxyMapping::getTargetConnections));
    }

    private static String getLocalServerAddress()  {
        InetAddress localHost = null;
        String ipAddress =null;
        try {
            localHost = InetAddress.getLocalHost();
            ipAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            log.info("can't get local server address'");
            ipAddress ="localhost";
        }
        return ipAddress;
    }

    @PostMapping("/addProxyMapping")
    public void addProxyMapping(@RequestBody TcpProxyMapping mapping) {
        int insert = mappingMapper.insert(mapping);
    }

    @PostMapping("/reload/{env}")
    public void reload(@PathVariable String env) throws Exception {
        LockUtils.executeWithLock(getSeverAddress()+Constants.RELOAD_EMN_KEY, 10l, v -> {
            try {
                reloadSupplier(env);
            } catch (Exception e) {
                log.info("Failed to reload,exception: ", e);
            }
        });
    }

    private void reloadSupplier(String env) throws Exception {
        log.info("reloading env: {} start ", env);
        Long start = System.currentTimeMillis();
        if (server == null) {
            log.info("server is null ,start netty server");
            start(env);
        } else {
            if (env.equals(ProxyConfigEnum.RUNTIME_ENV.getKeyVal(proxyConfigMapper))) {
                Map<String, List<String[]>> hostsByEmv = getHostsByEmv(env);
                Map<String, Channel> serverChannels = server.getServerChannels();
                Map<String, ConcurrentLinkedQueue<ProxyHandler>> targetProxyHandlerForHosts = server.getTargetProxyHandlerForHosts();
                if (CollectionUtil.isEmpty(hostsByEmv)) {
                    for (String port : serverChannels.keySet()) {
                        server.closeChannelConnects(port);
                    }
                    server.setHosts(new ConcurrentHashMap<>());
                    return;
                }
                log.info("refresh {} config：{}", env, JSON.toJSONString(hostsByEmv));
                //处理新启的服务端
                Map<String, List<String[]>> needStartServer = hostsByEmv.entrySet().stream().filter(entry -> !serverChannels.keySet().contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                List<String> needStopServerPort = server.getHosts().keySet().stream().filter(port -> !hostsByEmv.keySet().contains(port)).collect(Collectors.toList());
                //处理关闭的服务端
                for (String port : needStopServerPort) {
                    server.closeChannelConnects(port);
                }
                server.start(server, needStartServer);
                server.setHosts(hostsByEmv);
                closeNonConfig(hostsByEmv, targetProxyHandlerForHosts);
            } else {
                changeEnv(env);
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
            //
            targetProxyHandlerForHosts.get(stringListEntry.getKey()).stream().filter(proxyHandler ->
                    !stringListEntry.getValue().stream().anyMatch(array -> array[1].equals(proxyHandler.getTargetServerAddress()))
            ).forEach(ProxyHandler::initiativeReconnected);
        }
    }

    @GetMapping("/stopTarget/{hostPort}/{targetHost}")
    public void stopTarget(@PathVariable String hostPort, @PathVariable String targetHost) throws Exception {
        server.stopTargetServer(hostPort, targetHost);
    }
}
