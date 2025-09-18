package com.ppcex.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 风控服务启动类
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication(scanBasePackages = "com.ppcex")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ppcex")
@EnableAsync
@EnableScheduling
public class RiskServiceApplication {

    /**
     * 主启动方法
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RiskServiceApplication.class, args);
        System.out.println("==========================================");
        System.out.println("    PPCEX Risk Service Started Successfully");
        System.out.println("==========================================");
    }
}