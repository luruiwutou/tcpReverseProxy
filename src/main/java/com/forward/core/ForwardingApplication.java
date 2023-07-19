package com.forward.core;

import io.netty.util.ResourceLeakDetector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"com.forward.core"})
@MapperScan({"com.forward.core.tcpReverseProxy.mapper"})
public class ForwardingApplication {

    public static void main(String[] args) {
        // 设置资源泄露检测器的级别为 PARANOID
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);
        SpringApplication.run(ForwardingApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
