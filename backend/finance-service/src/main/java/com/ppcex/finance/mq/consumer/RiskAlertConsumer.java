package com.ppcex.finance.mq.consumer;

import com.ppcex.finance.entity.RiskEvent;
import com.ppcex.finance.mq.message.RiskAlertMessage;
import com.ppcex.finance.repository.RiskEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RocketMQMessageListener(
        topic = "risk-topic",
        selectorExpression = "RISK_ALERT",
        consumerGroup = "finance-risk-consumer",
        messageModel = org.apache.rocketmq.common.protocol.heartbeat.MessageModel.CLUSTERING,
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY
)
public class RiskAlertConsumer implements RocketMQListener<Message<RiskAlertMessage>> {

    private final RiskEventRepository riskEventRepository;

    public RiskAlertConsumer(RiskEventRepository riskEventRepository) {
        this.riskEventRepository = riskEventRepository;
    }

    @Override
    public void onMessage(Message<RiskAlertMessage> message) {
        try {
            RiskAlertMessage alertMessage = message.getPayload();

            log.info("处理风控告警消息: eventNo={}, userId={}, riskLevel={}",
                    alertMessage.getEventNo(), alertMessage.getUserId(), alertMessage.getRiskLevel());

            // 验证消息完整性
            validateRiskAlertMessage(alertMessage);

            // 创建风险事件记录
            RiskEvent event = new RiskEvent();
            event.setEventNo(alertMessage.getEventNo());
            event.setUserId(alertMessage.getUserId());
            event.setEventType(RiskEvent.EventType.BEHAVIOR_ABNORMAL.getCode());
            event.setRiskLevel(alertMessage.getRiskLevel());
            event.setEventContent("风控告警: " + String.join(", ", alertMessage.getTriggeredRules()));
            event.setClientIp(alertMessage.getClientIp());
            event.setDeviceInfo(alertMessage.getDeviceInfo());
            event.setLocation(alertMessage.getLocation());
            event.setAmount(alertMessage.getAmount());
            event.setCoinId(alertMessage.getCoinId());
            event.setRuleTriggered(String.join(", ", alertMessage.getTriggeredRules()));
            event.setActionTaken(alertMessage.getActionRequired());
            event.setStatus(RiskEvent.Status.PENDING.getCode());
            event.setCreateTime(LocalDateTime.now());
            event.setUpdateTime(LocalDateTime.now());

            riskEventRepository.insert(event);

            log.info("风控告警消息处理成功: eventNo={}, eventId={}",
                    alertMessage.getEventNo(), event.getId());

            // 执行风控处理逻辑
            handleRiskAction(alertMessage);

        } catch (Exception e) {
            log.error("处理风控告警消息失败", e);
            throw new RuntimeException("风控告警消息处理失败", e);
        }
    }

    private void validateRiskAlertMessage(RiskAlertMessage message) {
        if (message.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (message.getRiskLevel() == null) {
            throw new IllegalArgumentException("风险等级不能为空");
        }
        if (message.getTriggeredRules() == null || message.getTriggeredRules().isEmpty()) {
            throw new IllegalArgumentException("触发规则不能为空");
        }
        if (message.getEventNo() == null || message.getEventNo().trim().isEmpty()) {
            throw new IllegalArgumentException("事件编号不能为空");
        }
    }

    private void handleRiskAction(RiskAlertMessage alertMessage) {
        try {
            // 根据风险等级执行不同的处理逻辑
            switch (alertMessage.getRiskLevel()) {
                case 4: // 严重风险
                    log.warn("处理严重风险事件: userId={}, action={}", alertMessage.getUserId(), alertMessage.getActionRequired());
                    // 发送紧急告警通知
                    sendEmergencyAlert(alertMessage);
                    break;
                case 3: // 高风险
                    log.warn("处理高风险事件: userId={}, action={}", alertMessage.getUserId(), alertMessage.getActionRequired());
                    // 发送告警通知
                    sendAlert(alertMessage);
                    break;
                case 2: // 中等风险
                    log.info("处理中等风险事件: userId={}, action={}", alertMessage.getUserId(), alertMessage.getActionRequired());
                    // 记录日志
                    break;
                case 1: // 低风险
                    log.debug("处理低风险事件: userId={}, action={}", alertMessage.getUserId(), alertMessage.getActionRequired());
                    // 仅记录日志
                    break;
                default:
                    log.warn("未知风险等级: {}", alertMessage.getRiskLevel());
            }
        } catch (Exception e) {
            log.error("执行风控处理失败: eventNo={}", alertMessage.getEventNo(), e);
        }
    }

    private void sendAlert(RiskAlertMessage alertMessage) {
        // 实现告警发送逻辑
        log.info("发送风控告警: userId={}, riskLevel={}, rules={}",
                alertMessage.getUserId(), alertMessage.getRiskLevel(), alertMessage.getTriggeredRules());
    }

    private void sendEmergencyAlert(RiskAlertMessage alertMessage) {
        // 实现紧急告警发送逻辑
        log.error("发送紧急风控告警: userId={}, riskLevel={}, rules={}",
                alertMessage.getUserId(), alertMessage.getRiskLevel(), alertMessage.getTriggeredRules());
    }
}