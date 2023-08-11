package com.forward.core.tcpReverseProxy.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MybatisConfig {
    @Bean
    StringArrayListTypeHandler handler() {
        return new StringArrayListTypeHandler();
    }
}
