package com.forward.core.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HsmClientProperties.class)
public class HsmClientConfig {
    @Autowired
    private HsmClientProperties hsmClientProperties;

//    @Bean
//    public HsmSendClient hsmPoolClient() {
//        return new HsmSendClient(hsmClientProperties);
//    }

}
