package com.ppcex.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel配置类
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SentinelConfig {

    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 用户服务限流规则
        GatewayFlowRule userRule = new GatewayFlowRule("user-service")
                .setCount(1000) // QPS阈值
                .setIntervalSec(1) // 统计时间间隔，单位是秒
                .setBurst(200) // 突发流量
                .setControlBehavior(0); // 直接拒绝

        // 交易服务限流规则
        GatewayFlowRule tradeRule = new GatewayFlowRule("trade-service")
                .setCount(500) // QPS阈值
                .setIntervalSec(1) // 统计时间间隔，单位是秒
                .setBurst(100) // 突发流量
                .setControlBehavior(0); // 直接拒绝

        // 钱包服务限流规则
        GatewayFlowRule walletRule = new GatewayFlowRule("wallet-service")
                .setCount(200) // QPS阈值
                .setIntervalSec(1) // 统计时间间隔，单位是秒
                .setBurst(50) // 突发流量
                .setControlBehavior(0); // 直接拒绝

        // 行情服务限流规则
        GatewayFlowRule marketRule = new GatewayFlowRule("market-service")
                .setCount(5000) // QPS阈值
                .setIntervalSec(1) // 统计时间间隔，单位是秒
                .setBurst(1000) // 突发流量
                .setControlBehavior(1); // 匀速排队

        // 财务服务限流规则
        GatewayFlowRule financeRule = new GatewayFlowRule("finance-service")
                .setCount(300) // QPS阈值
                .setIntervalSec(1) // 统计时间间隔，单位是秒
                .setBurst(50) // 突发流量
                .setControlBehavior(0); // 直接拒绝

        // 风控服务限流规则
        GatewayFlowRule riskRule = new GatewayFlowRule("risk-service")
                .setCount(800) // QPS阈值
                .setIntervalSec(1) // 统计时间间隔，单位是秒
                .setBurst(100) // 突发流量
                .setControlBehavior(0); // 直接拒绝

        rules.add(userRule);
        rules.add(tradeRule);
        rules.add(walletRule);
        rules.add(marketRule);
        rules.add(financeRule);
        rules.add(riskRule);

        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel gateway rules loaded: {}", rules.size());

        // 初始化熔断规则
        initDegradeRules();
    }

    /**
     * 初始化熔断规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 用户服务熔断规则
        DegradeRule userRule = new DegradeRule("user-service")
                .setGrade(1) // 异常比例熔断 (1: 异常比例, 0: 异常数)
                .setCount(0.3) // 30%异常率触发熔断
                .setTimeWindow(60) // 熔断持续时间60秒
                .setMinRequestAmount(10) // 最小请求数
                .setSlowRatioThreshold(0.8); // 慢调用比例80%

        // 交易服务熔断规则
        DegradeRule tradeRule = new DegradeRule("trade-service")
                .setGrade(1)
                .setCount(0.5) // 50%异常率触发熔断
                .setTimeWindow(30) // 熔断持续时间30秒
                .setMinRequestAmount(5)
                .setSlowRatioThreshold(0.5);

        // 钱包服务熔断规则
        DegradeRule walletRule = new DegradeRule("wallet-service")
                .setGrade(1)
                .setCount(0.2) // 20%异常率触发熔断
                .setTimeWindow(120) // 熔断持续时间120秒
                .setMinRequestAmount(3)
                .setSlowRatioThreshold(0.2);

        // 财务服务熔断规则
        DegradeRule financeRule = new DegradeRule("finance-service")
                .setGrade(1)
                .setCount(0.4) // 40%异常率触发熔断
                .setTimeWindow(60) // 熔断持续时间60秒
                .setMinRequestAmount(5)
                .setSlowRatioThreshold(0.6);

        // 风控服务熔断规则
        DegradeRule riskRule = new DegradeRule("risk-service")
                .setGrade(1)
                .setCount(0.6) // 60%异常率触发熔断
                .setTimeWindow(30) // 熔断持续时间30秒
                .setMinRequestAmount(8)
                .setSlowRatioThreshold(0.7);

        // 行情服务不启用熔断，保证数据可用性
        // DegradeRule marketRule = new DegradeRule("market-service")
        //         .setGrade(1)
        //         .setCount(0.8)
        //         .setTimeWindow(10)
        //         .setMinRequestAmount(20);

        rules.add(userRule);
        rules.add(tradeRule);
        rules.add(walletRule);
        rules.add(financeRule);
        rules.add(riskRule);

        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel degrade rules loaded: {}", rules.size());
    }

    /**
     * Sentinel网关过滤器
     */
    @Bean
    public SentinelGatewayFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 配置限流处理器
     */
    @PostConstruct
    public void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 429);
                result.put("message", "请求过于频繁，请稍后重试");
                result.put("timestamp", System.currentTimeMillis());

                if (t instanceof BlockException) {
                    log.warn("Request blocked by Sentinel: {} {}",
                        exchange.getRequest().getPath(),
                        ((BlockException) t).getRule());
                }

                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(JSON.toJSONString(result)));
            }
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
        log.info("Sentinel block handler initialized");
    }
}