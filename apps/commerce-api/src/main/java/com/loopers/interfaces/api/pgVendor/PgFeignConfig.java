package com.loopers.interfaces.api.pgVendor;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PgFeignConfig {
    /**
     * LoadBalancer 안 타는 기본 Feign Client
     */
    @Bean
    public Client feignClient() {
        // 첫 번째 인자: SSLSocketFactory (null이면 기본)
        // 두 번째 인자: HostnameVerifier (null이면 기본)
        return new Client.Default(null, null);
    }
}
