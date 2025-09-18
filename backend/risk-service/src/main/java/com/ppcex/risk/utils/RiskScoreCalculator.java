package com.ppcex.risk.utils;

import com.ppcex.risk.constant.RiskConstants;
import com.ppcex.risk.dto.RiskCheckRequest;
import com.ppcex.risk.entity.RiskRule;
import com.ppcex.risk.entity.UserRiskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 风险评分计算器
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class RiskScoreCalculator {

    /**
     * 计算风险评分
     *
     * @param request 风控检查请求
     * @param triggeredRules 触发的规则列表
     * @param userRiskStatus 用户风控状态
     * @return 风险评分
     */
    public int calculate(RiskCheckRequest request, List<RiskRule> triggeredRules, UserRiskStatus userRiskStatus) {
        try {
            // 计算基础评分
            double baseScore = calculateBaseScore(request, userRiskStatus);

            // 计算行为评分
            double behaviorScore = calculateBehaviorScore(request, triggeredRules);

            // 计算上下文评分
            double contextScore = calculateContextScore(request);

            // 计算历史评分
            double historyScore = calculateHistoryScore(userRiskStatus);

            // 计算时间衰减因子
            double timeDecayFactor = calculateTimeDecayFactor(userRiskStatus);

            // 加权计算最终评分
            double totalScore = baseScore * RiskConstants.Scoring.BASE_WEIGHT +
                    behaviorScore * RiskConstants.Scoring.BEHAVIOR_WEIGHT +
                    contextScore * RiskConstants.Scoring.CONTEXT_WEIGHT +
                    historyScore * RiskConstants.Scoring.HISTORY_WEIGHT;

            // 应用时间衰减因子
            totalScore = totalScore * timeDecayFactor;

            // 确保评分在有效范围内
            int finalScore = (int) Math.round(totalScore);
            return Math.max(RiskConstants.Scoring.MIN_SCORE, Math.min(RiskConstants.Scoring.MAX_SCORE, finalScore));

        } catch (Exception e) {
            log.error("计算风险评分失败，用户ID: {}", request.getUserId(), e);
            return RiskConstants.Scoring.MIN_SCORE;
        }
    }

    /**
     * 计算基础评分
     */
    private double calculateBaseScore(RiskCheckRequest request, UserRiskStatus userRiskStatus) {
        double score = 0;

        // 用户类型评分
        if (userRiskStatus != null) {
            // 基于用户风控状态评分
            switch (userRiskStatus.getStatus()) {
                case RiskConstants.UserRiskStatus.MONITOR:
                    score += 20;
                    break;
                case RiskConstants.UserRiskStatus.LIMIT:
                    score += 40;
                    break;
                case RiskConstants.UserRiskStatus.FREEZE:
                    score += 80;
                    break;
                default:
                    score += 0;
            }

            // 基于历史风险评分
            if (userRiskStatus.getRiskScore() != null) {
                score += userRiskStatus.getRiskScore() * 0.3;
            }

            // 基于风控事件数量
            if (userRiskStatus.getRiskEventCount() != null) {
                score += Math.min(30, userRiskStatus.getRiskEventCount() * 2);
            }
        }

        // 新用户评分
        if (userRiskStatus == null || userRiskStatus.getCreateTime() == null) {
            score += 10;
        }

        return Math.min(50, score);
    }

    /**
     * 计算行为评分
     */
    private double calculateBehaviorScore(RiskCheckRequest request, List<RiskRule> triggeredRules) {
        double score = 0;

        // 基于触发的规则评分
        for (RiskRule rule : triggeredRules) {
            switch (rule.getRiskLevel()) {
                case RiskConstants.RiskLevel.LOW:
                    score += 5;
                    break;
                case RiskConstants.RiskLevel.MEDIUM:
                    score += 15;
                    break;
                case RiskConstants.RiskLevel.HIGH:
                    score += 30;
                    break;
                case RiskConstants.RiskLevel.SEVERE:
                    score += 50;
                    break;
            }
        }

        // 基于事件类型评分
        switch (request.getEventType()) {
            case RiskConstants.EventType.LOGIN:
                score += 2;
                break;
            case RiskConstants.EventType.TRADE:
                score += 8;
                break;
            case RiskConstants.EventType.RECHARGE:
                score += 5;
                break;
            case RiskConstants.EventType.WITHDRAW:
                score += 15;
                break;
            case RiskConstants.EventType.REGISTER:
                score += 1;
                break;
            default:
                score += 3;
        }

        // 基于异常IP评分
        if (isSuspiciousIp(request.getIpAddress())) {
            score += 20;
        }

        // 基于异常设备评分
        if (isSuspiciousDevice(request.getDeviceInfo())) {
            score += 15;
        }

        return Math.min(50, score);
    }

    /**
     * 计算上下文评分
     */
    private double calculateContextScore(RiskCheckRequest request) {
        double score = 0;

        // 时间评分
        if (isSuspiciousTime(request.getRequestTimestamp())) {
            score += 10;
        }

        // 位置评分
        if (isSuspiciousLocation(request.getIpAddress())) {
            score += 15;
        }

        // 频率评分
        if (isHighFrequency(request)) {
            score += 20;
        }

        // 金额评分
        if (isLargeAmount(request)) {
            score += 25;
        }

        return Math.min(30, score);
    }

    /**
     * 计算历史评分
     */
    private double calculateHistoryScore(UserRiskStatus userRiskStatus) {
        if (userRiskStatus == null) {
            return 0;
        }

        double score = 0;

        // 基于失败登录次数
        if (userRiskStatus.getFailedLoginCount() != null) {
            score += Math.min(20, userRiskStatus.getFailedLoginCount() * 3);
        }

        // 基于可疑IP数量
        if (userRiskStatus.getSuspiciousIpCount() != null) {
            score += Math.min(15, userRiskStatus.getSuspiciousIpCount() * 2);
        }

        // 基于最后风控时间
        if (userRiskStatus.getLastRiskTime() != null) {
            long daysSinceLastRisk = ChronoUnit.DAYS.between(userRiskStatus.getLastRiskTime(), LocalDateTime.now());
            if (daysSinceLastRisk <= 7) {
                score += 10;
            } else if (daysSinceLastRisk <= 30) {
                score += 5;
            }
        }

        return Math.min(20, score);
    }

    /**
     * 计算时间衰减因子
     */
    private double calculateTimeDecayFactor(UserRiskStatus userRiskStatus) {
        if (userRiskStatus == null || userRiskStatus.getLastRiskTime() == null) {
            return 1.0;
        }

        long daysSinceLastRisk = ChronoUnit.DAYS.between(userRiskStatus.getLastRiskTime(), LocalDateTime.now());

        if (daysSinceLastRisk <= 1) {
            return 1.0;
        } else if (daysSinceLastRisk <= 7) {
            return 0.9;
        } else if (daysSinceLastRisk <= 30) {
            return 0.8;
        } else if (daysSinceLastRisk <= 90) {
            return 0.7;
        } else {
            return 0.6;
        }
    }

    /**
     * 判断是否为可疑IP
     */
    private boolean isSuspiciousIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return true;
        }

        // 检查是否为内网IP
        if (isInternalIp(ipAddress)) {
            return false;
        }

        // 检查是否为代理IP
        if (isProxyIp(ipAddress)) {
            return true;
        }

        // 检查是否为高风险地区IP
        if (isHighRiskRegion(ipAddress)) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为可疑设备
     */
    private boolean isSuspiciousDevice(String deviceInfo) {
        if (deviceInfo == null || deviceInfo.trim().isEmpty()) {
            return true;
        }

        // 检查是否为虚拟机
        if (deviceInfo.toLowerCase().contains("vmware") ||
            deviceInfo.toLowerCase().contains("virtual") ||
            deviceInfo.toLowerCase().contains("qemu")) {
            return true;
        }

        // 检查是否为自动化工具
        if (deviceInfo.toLowerCase().contains("bot") ||
            deviceInfo.toLowerCase().contains("crawler") ||
            deviceInfo.toLowerCase().contains("spider")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为可疑时间
     */
    private boolean isSuspiciousTime(Long timestamp) {
        if (timestamp == null) {
            return false;
        }

        LocalDateTime requestTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, null);
        int hour = requestTime.getHour();

        // 凌晨2-6点为可疑时间段
        return hour >= 2 && hour <= 6;
    }

    /**
     * 判断是否为可疑位置
     */
    private boolean isSuspiciousLocation(String ipAddress) {
        // 简化实现，实际应根据IP地理位置判断
        return false;
    }

    /**
     * 判断是否为高频操作
     */
    private boolean isHighFrequency(RiskCheckRequest request) {
        // 简化实现，实际应根据用户历史行为判断
        return false;
    }

    /**
     * 判断是否为大额操作
     */
    private boolean isLargeAmount(RiskCheckRequest request) {
        if (request.getEventData() == null) {
            return false;
        }

        try {
            Object amountObj = request.getEventData().get("amount");
            if (amountObj instanceof BigDecimal) {
                BigDecimal amount = (BigDecimal) amountObj;
                return amount.compareTo(new BigDecimal("10000")) > 0;
            } else if (amountObj instanceof Number) {
                double amount = ((Number) amountObj).doubleValue();
                return amount > 10000;
            }
        } catch (Exception e) {
            log.warn("解析金额失败", e);
        }

        return false;
    }

    /**
     * 判断是否为内网IP
     */
    private boolean isInternalIp(String ipAddress) {
        return ipAddress.startsWith("192.168.") ||
               ipAddress.startsWith("10.") ||
               ipAddress.startsWith("172.16.") ||
               ipAddress.startsWith("172.17.") ||
               ipAddress.startsWith("172.18.") ||
               ipAddress.startsWith("172.19.") ||
               ipAddress.startsWith("172.2") ||
               ipAddress.startsWith("172.30.") ||
               ipAddress.startsWith("172.31.") ||
               "127.0.0.1".equals(ipAddress) ||
               "localhost".equals(ipAddress);
    }

    /**
     * 判断是否为代理IP
     */
    private boolean isProxyIp(String ipAddress) {
        // 简化实现，实际应根据代理IP数据库判断
        return false;
    }

    /**
     * 判断是否为高风险地区IP
     */
    private boolean isHighRiskRegion(String ipAddress) {
        // 简化实现，实际应根据IP地理位置数据库判断
        return false;
    }
}