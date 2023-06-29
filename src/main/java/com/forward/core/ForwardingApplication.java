package com.forward.core;

import com.forward.core.netty.server.NettyForwardServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class ForwardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForwardingApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
//    @Autowired
//    private NettyForwardServer relayServer;
//
//    @PreDestroy
//    public void stopRelayServer() {
//        relayServer.stop();
//    }
}
