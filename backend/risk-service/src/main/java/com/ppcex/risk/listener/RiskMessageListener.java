package com.ppcex.risk.listener;

import com.alibaba.fastjson2.JSON;
import com.ppcex.risk.dto.RiskCheckRequest;
import com.ppcex.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 风控消息监听器
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskMessageListener {

    private final RiskService riskService;

    /**
     * 监听风控主题消息
     */
    @StreamListener("riskTopic-in-0")
    public void handleRiskTopic(Message<String> message) {
        try {
            String payload = message.getPayload();
            log.debug("收到风控主题消息: {}", payload);

            Map<String, Object> event = JSON.parseObject(payload, Map.class);
            String tag = (String) event.get("tag");
            String eventType = (String) event.get("eventType");

            // 处理不同类型的消息
            switch (eventType) {
                case "RISK_EVENT":
                    handleRiskEvent(event);
                    break;
                case "RULE_UPDATE":
                    handleRuleUpdate(event);
                    break;
                case "STRATEGY_UPDATE":
                    handleStrategyUpdate(event);
                    break;
                default:
                    log.warn("未知的消息类型: {}", eventType);
            }

        } catch (Exception e) {
            log.error("处理风控消息失败", e);
        }
    }

    /**
     * 监听用户主题消息
     */
    @StreamListener("userTopic-in-0")
    public void handleUserTopic(Message<String> message) {
        try {
            String payload = message.getPayload();
            log.debug("收到用户主题消息: {}", payload);

            Map<String, Object> event = JSON.parseObject(payload, Map.class);
            String eventType = (String) event.get("eventType");

            // 处理用户相关消息
            switch (eventType) {
                case "USER_REGISTER":
                    handleUserRegister(event);
                    break;
                case "USER_LOGIN":
                    handleUserLogin(event);
                    break;
                case "USER_KYC":
                    handleUserKyc(event);
                    break;
                case "USER_UPDATE":
                    handleUserUpdate(event);
                    break;
                default:
                    log.warn("未知的用户消息类型: {}", eventType);
            }

        } catch (Exception e) {
            log.error("处理用户消息失败", e);
        }
    }

    /**
     * 监听交易主题消息
     */
    @StreamListener("tradeTopic-in-0")
    public void handleTradeTopic(Message<String> message) {
        try {
            String payload = message.getPayload();
            log.debug("收到交易主题消息: {}", payload);

            Map<String, Object> event = JSON.parseObject(payload, Map.class);
            String eventType = (String) event.get("eventType");

            // 处理交易相关消息
            switch (eventType) {
                case "ORDER_CREATE":
                    handleOrderCreate(event);
                    break;
                case "ORDER_TRADE":
                    handleOrderTrade(event);
                    break;
                case "ORDER_CANCEL":
                    handleOrderCancel(event);
                    break;
                case "ORDER_FAILED":
                    handleOrderFailed(event);
                    break;
                default:
                    log.warn("未知的交易消息类型: {}", eventType);
            }

        } catch (Exception e) {
            log.error("处理交易消息失败", e);
        }
    }

    /**
     * 监听资产主题消息
     */
    @StreamListener("assetTopic-in-0")
    public void handleAssetTopic(Message<String> message) {
        try {
            String payload = message.getPayload();
            log.debug("收到资产主题消息: {}", payload);

            Map<String, Object> event = JSON.parseObject(payload, Map.class);
            String eventType = (String) event.get("eventType");

            // 处理资产相关消息
            switch (eventType) {
                case "ASSET_RECHARGE":
                    handleAssetRecharge(event);
                    break;
                case "ASSET_WITHDRAW":
                    handleAssetWithdraw(event);
                    break;
                case "ASSET_TRANSFER":
                    handleAssetTransfer(event);
                    break;
                case "ASSET_FREEZE":
                    handleAssetFreeze(event);
                    break;
                default:
                    log.warn("未知的资产消息类型: {}", eventType);
            }

        } catch (Exception e) {
            log.error("处理资产消息失败", e);
        }
    }

    /**
     * 处理风控事件
     */
    private void handleRiskEvent(Map<String, Object> event) {
        log.info("处理风控事件: {}", event);
        // 处理风控相关事件
    }

    /**
     * 处理规则更新
     */
    private void handleRuleUpdate(Map<String, Object> event) {
        log.info("处理规则更新: {}", event);
        // 刷新规则缓存
    }

    /**
     * 处理策略更新
     */
    private void handleStrategyUpdate(Map<String, Object> event) {
        log.info("处理策略更新: {}", event);
        // 刷新策略缓存
    }

    /**
     * 处理用户注册
     */
    private void handleUserRegister(Map<String, Object> event) {
        log.info("处理用户注册: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.REGISTER);
            request.setBusinessType("USER_REGISTER");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理用户登录
     */
    private void handleUserLogin(Map<String, Object> event) {
        log.info("处理用户登录: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.LOGIN);
            request.setBusinessType("USER_LOGIN");
            request.setIpAddress(getStringValue(event, "ipAddress"));
            request.setDeviceInfo(getStringValue(event, "deviceInfo"));
            request.setUserAgent(getStringValue(event, "userAgent"));
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理用户KYC
     */
    private void handleUserKyc(Map<String, Object> event) {
        log.info("处理用户KYC: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.OTHER);
            request.setBusinessType("USER_KYC");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理用户更新
     */
    private void handleUserUpdate(Map<String, Object> event) {
        log.info("处理用户更新: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.OTHER);
            request.setBusinessType("USER_UPDATE");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理订单创建
     */
    private void handleOrderCreate(Map<String, Object> event) {
        log.info("处理订单创建: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.TRADE);
            request.setBusinessType("ORDER_CREATE");
            request.setIpAddress(getStringValue(event, "ipAddress"));
            request.setDeviceInfo(getStringValue(event, "deviceInfo"));
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理订单成交
     */
    private void handleOrderTrade(Map<String, Object> event) {
        log.info("处理订单成交: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.TRADE);
            request.setBusinessType("ORDER_TRADE");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理订单取消
     */
    private void handleOrderCancel(Map<String, Object> event) {
        log.info("处理订单取消: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.TRADE);
            request.setBusinessType("ORDER_CANCEL");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理订单失败
     */
    private void handleOrderFailed(Map<String, Object> event) {
        log.info("处理订单失败: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.TRADE);
            request.setBusinessType("ORDER_FAILED");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理资产充值
     */
    private void handleAssetRecharge(Map<String, Object> event) {
        log.info("处理资产充值: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.RECHARGE);
            request.setBusinessType("ASSET_RECHARGE");
            request.setIpAddress(getStringValue(event, "ipAddress"));
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理资产提现
     */
    private void handleAssetWithdraw(Map<String, Object> event) {
        log.info("处理资产提现: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.WITHDRAW);
            request.setBusinessType("ASSET_WITHDRAW");
            request.setIpAddress(getStringValue(event, "ipAddress"));
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理资产转账
     */
    private void handleAssetTransfer(Map<String, Object> event) {
        log.info("处理资产转账: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.OTHER);
            request.setBusinessType("ASSET_TRANSFER");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 处理资产冻结
     */
    private void handleAssetFreeze(Map<String, Object> event) {
        log.info("处理资产冻结: {}", event);

        Long userId = getLongValue(event, "userId");
        if (userId != null) {
            RiskCheckRequest request = new RiskCheckRequest();
            request.setUserId(userId);
            request.setEventType(com.ppcex.risk.constant.RiskConstants.EventType.OTHER);
            request.setBusinessType("ASSET_FREEZE");
            request.setEventData(event);
            request.setRequestTimestamp(System.currentTimeMillis());

            riskService.checkRisk(request);
        }
    }

    /**
     * 获取Long类型值
     */
    private Long getLongValue(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取String类型值
     */
    private String getStringValue(Map<String, Object> event, String key) {
        Object value = event.get(key);
        return value != null ? value.toString() : null;
    }
}