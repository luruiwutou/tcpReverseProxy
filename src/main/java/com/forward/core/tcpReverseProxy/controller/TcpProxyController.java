package com.forward.core.tcpReverseProxy.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.forward.core.tcpReverseProxy.ReverseProxyServer;
import com.forward.core.tcpReverseProxy.entity.TcpProxyMapping;
import com.forward.core.tcpReverseProxy.enums.ProxyConfigEnum;
import com.forward.core.tcpReverseProxy.mapper.ProxyConfigMapper;
import com.forward.core.tcpReverseProxy.mapper.TcpProxyMappingMapper;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/proxy")
@Slf4j
public class TcpProxyController {
    @Autowired
    TcpProxyMappingMapper mappingMapper;
    @Autowired
    ProxyConfigMapper proxyConfigMapper;


    private ReverseProxyServer server;
    @PostConstruct
    public void init() {
        // 在应用加载完成后执行的初始化方法逻辑
        try {
            log.info("Start Tcp Proxy Server");
            start(ProxyConfigEnum.DEFAULT_ENV.getKeyVal(proxyConfigMapper));
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Tcp proxy started failed: {}" ,e.getMessage());
        }
    }

    @GetMapping("/start/{env}")
    public void start(@PathVariable String env) throws Exception {
        Map<String, List<String>> hosts = getHostsByEmv(env);
        server = ReverseProxyServer.start(hosts);
        ProxyConfigEnum.RUNTIME_ENV.update(env, proxyConfigMapper);
//        reverseProxyServer.bootstrap()
    }

    @GetMapping("/changeEnv/{env}")
    public void changeEnv(@PathVariable String env) throws Exception {
        Map<String, List<String>> emvHosts = getHostsByEmv(env);
        Map<String, List<String>> hosts = server.getHosts();
        List<String> needStartServerPort = emvHosts.keySet().stream().filter(hosts.keySet()::contains).collect(Collectors.toList());
        List<String> needStopServerPort = hosts.keySet().stream().filter(emvHosts.keySet()::contains).collect(Collectors.toList());
        Map<String, List<String>> needStartServer = emvHosts.entrySet().stream().filter(entry -> needStartServerPort.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

    }

    public static void main(String[] args) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            System.out.println("IP 地址: " + ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, List<String>> getHostsByEmv(@PathVariable String env) throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        String ipAddress = localHost.getHostAddress();
        List<TcpProxyMapping> tcpProxyMappings = mappingMapper.selectMappingWithTargets(env, ipAddress);
        return tcpProxyMappings.stream().collect(Collectors.toMap(TcpProxyMapping::getLocalPort, TcpProxyMapping::getTargetConnections));
    }

    @PostMapping("/addProxyMapping")
    public void addProxyMapping(@RequestBody TcpProxyMapping mapping) {
        int insert = mappingMapper.insert(mapping);
    }

    @PostMapping("/reload/{env}")
    public void reload(@PathVariable String env) throws Exception {
        if (server == null) {
            start(env);
        } else {
            if (env.equals(ProxyConfigEnum.RUNTIME_ENV.getKeyVal(proxyConfigMapper))) {
                Map<String, List<String>> hostsByEmv = getHostsByEmv(env);
                Map<String, Channel> serverChannels = server.getServerChannels();
                Map<String, List<String>> needStartServer = hostsByEmv.entrySet().stream().filter(entry -> !serverChannels.keySet().contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                server.start(server, needStartServer);
                List<String> needStopServerPort = server.getHosts().keySet().stream().filter(port -> !hostsByEmv.keySet().contains(port)).collect(Collectors.toList());
                for (String port : needStopServerPort) {
                    server.closeChannelConnects(port);
                }
                server.setHosts(hostsByEmv);
            } else {
                changeEnv(env);
            }
        }

    }

    @GetMapping("/stopTarget")
    public void stopTarget() throws Exception {
        server.stopTargetServer("9001", "localhost:8882");
    }
}
