package com.ppcex.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ppcex.finance.dto.RiskCheckRequest;
import com.ppcex.finance.dto.RiskCheckResult;
import com.ppcex.finance.entity.RiskControl;
import com.ppcex.finance.entity.RiskEvent;
import com.ppcex.finance.repository.RiskControlRepository;
import com.ppcex.finance.repository.RiskEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlService {

    private final RiskControlRepository riskControlRepository;
    private final RiskEventRepository riskEventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public RiskCheckResult checkRisk(RiskCheckRequest request) {
        log.info("执行风控检查: userId={}, ip={}, amount={}, businessType={}",
                request.getUserId(), request.getClientIp(), request.getAmount(), request.getBusinessType());

        List<String> triggeredRules = new ArrayList<>();
        int maxRiskLevel = 1;

        // 获取所有启用的风控规则
        List<RiskControl> rules = riskControlRepository.getEnabledRules();

        for (RiskControl rule : rules) {
            try {
                RiskCheckResult ruleResult = checkSingleRule(rule, request);
                if (!ruleResult.getPass()) {
                    triggeredRules.add(rule.getRuleName());
                    maxRiskLevel = Math.max(maxRiskLevel, ruleResult.getRiskLevel());
                }
            } catch (Exception e) {
                log.error("风控规则检查失败: rule={}", rule.getRuleName(), e);
            }
        }

        // 判断最终结果
        if (triggeredRules.isEmpty()) {
            return RiskCheckResult.pass();
        } else {
            // 记录风险事件
            recordRiskEvent(request, maxRiskLevel, triggeredRules);

            String message = "触发风控规则: " + String.join(", ", triggeredRules);
            return RiskCheckResult.fail(maxRiskLevel, message, triggeredRules);
        }
    }

    private RiskCheckResult checkSingleRule(RiskControl rule, RiskCheckRequest request) {
        switch (rule.getRuleType()) {
            case 1: // IP黑名单
                return checkIPBlacklist(rule, request);
            case 2: // 频率限制
                return checkRateLimit(rule, request);
            case 3: // 金额限制
                return checkAmountLimit(rule, request);
            case 5: // 地理位置风险
                return checkLocationRisk(rule, request);
            default:
                return RiskCheckResult.pass();
        }
    }

    private RiskCheckResult checkIPBlacklist(RiskControl rule, RiskCheckRequest request) {
        if (request.getClientIp() == null) {
            return RiskCheckResult.pass();
        }

        String cacheKey = "risk:ip:blacklist:" + request.getClientIp();
        Boolean isBlacklisted = (Boolean) redisTemplate.opsForValue().get(cacheKey);

        if (isBlacklisted != null && isBlacklisted) {
            return RiskCheckResult.fail(rule.getRiskLevel(), "IP在黑名单中", List.of(rule.getRuleName()));
        }

        return RiskCheckResult.pass();
    }

    private RiskCheckResult checkRateLimit(RiskControl rule, RiskCheckRequest request) {
        String rateLimitKey = String.format("risk:rate:%s:%s", request.getUserId(), request.getOperationType());

        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey);
        if (currentCount == 1) {
            redisTemplate.expire(rateLimitKey, 1, TimeUnit.MINUTES);
        }

        if (currentCount > rule.getThresholdValue().longValue()) {
            return RiskCheckResult.fail(rule.getRiskLevel(), "操作频率过高", List.of(rule.getRuleName()));
        }

        return RiskCheckResult.pass();
    }

    private RiskCheckResult checkAmountLimit(RiskControl rule, RiskCheckRequest request) {
        if (request.getAmount() == null) {
            return RiskCheckResult.pass();
        }

        if (request.getAmount().compareTo(rule.getThresholdValue()) > 0) {
            return RiskCheckResult.fail(rule.getRiskLevel(), "交易金额超过限制", List.of(rule.getRuleName()));
        }

        return RiskCheckResult.pass();
    }

    private RiskCheckResult checkLocationRisk(RiskControl rule, RiskCheckRequest request) {
        if (request.getLocation() == null) {
            return RiskCheckResult.pass();
        }

        // 简化版地理位置风险检查
        String[] highRiskCountries = {"CN", "KP", "IR", "SY", "CU"};
        for (String country : highRiskCountries) {
            if (request.getLocation().toUpperCase().contains(country)) {
                return RiskCheckResult.fail(rule.getRiskLevel(), "高风险地区", List.of(rule.getRuleName()));
            }
        }

        return RiskCheckResult.pass();
    }

    private void recordRiskEvent(RiskCheckRequest request, int riskLevel, List<String> triggeredRules) {
        try {
            RiskEvent event = new RiskEvent();
            event.setEventNo(generateEventNo());
            event.setUserId(request.getUserId());
            event.setEventType(RiskEvent.EventType.BEHAVIOR_ABNORMAL.getCode());
            event.setRiskLevel(riskLevel);
            event.setEventContent("触发风控规则: " + String.join(", ", triggeredRules));
            event.setClientIp(request.getClientIp());
            event.setDeviceInfo(request.getDeviceInfo());
            event.setLocation(request.getLocation());
            event.setAmount(request.getAmount());
            event.setCoinId(request.getCoinId());
            event.setRuleTriggered(String.join(", ", triggeredRules));
            event.setStatus(RiskEvent.Status.PENDING.getCode());
            event.setCreateTime(LocalDateTime.now());
            event.setUpdateTime(LocalDateTime.now());

            riskEventRepository.insert(event);
        } catch (Exception e) {
            log.error("记录风险事件失败", e);
        }
    }

    private String generateEventNo() {
        return "RISK" + System.currentTimeMillis();
    }

    public void addToIPBlacklist(String ip, String reason) {
        String cacheKey = "risk:ip:blacklist:" + ip;
        redisTemplate.opsForValue().set(cacheKey, true, 24, TimeUnit.HOURS);
        log.info("IP加入黑名单: ip={}, reason={}", ip, reason);
    }

    public void removeFromIPBlacklist(String ip) {
        String cacheKey = "risk:ip:blacklist:" + ip;
        redisTemplate.delete(cacheKey);
        log.info("IP移出黑名单: ip={}", ip);
    }
}