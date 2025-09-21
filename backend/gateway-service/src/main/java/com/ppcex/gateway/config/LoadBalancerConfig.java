package com.ppcex.gateway.config;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@LoadBalancerClients(defaultConfiguration = LoadBalancerConfig.class)
public class LoadBalancerConfig {
    // 简化配置，使用Spring Cloud LoadBalancer默认的round-robin策略
    // 如需自定义负载均衡策略，可以在具体的service配置中指定
}