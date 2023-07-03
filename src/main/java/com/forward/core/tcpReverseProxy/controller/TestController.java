package com.forward.core.tcpReverseProxy.controller;

import com.forward.core.tcpReverseProxy.ReverseProxyServer;
import com.forward.core.tcpReverseProxy.entity.TcpProxyMapping;
import com.forward.core.tcpReverseProxy.mapper.TcpProxyMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Autowired
    TcpProxyMappingMapper mappingMapper;


    private ReverseProxyServer server;

    @GetMapping("/start")
    public void start() throws Exception {
        List<TcpProxyMapping> tcpProxyMappings = mappingMapper.selectMappingWithTargets("sit");

        Map<String, List<String>> hosts = tcpProxyMappings.stream().collect(Collectors.toMap(TcpProxyMapping::getLocalPort, TcpProxyMapping::getTargetConnections));
        server = ReverseProxyServer.start(hosts);

//        reverseProxyServer.bootstrap()
    }

    @GetMapping("/changeEnv/{env}")
    public void changeUat(@PathVariable String env) throws Exception {
        List<TcpProxyMapping> tcpProxyMappings = mappingMapper.selectMappingWithTargets(env);
        Map<String, List<String>> uatHosts = tcpProxyMappings.stream().collect(Collectors.toMap(TcpProxyMapping::getLocalPort, TcpProxyMapping::getTargetConnections));
        Map<String, List<String>> hosts = server.getHosts();
        for (Map.Entry<String, List<String>> host : hosts.entrySet()) {
            server.stopTargetServer(host.getKey(), host.getValue());
        }
        server.setHosts(uatHosts);

    }

    @GetMapping("/stopTarget")
    public void stopTarget() throws Exception {
        server.stopTargetServer("9001", "localhost:8882");
    }
}
