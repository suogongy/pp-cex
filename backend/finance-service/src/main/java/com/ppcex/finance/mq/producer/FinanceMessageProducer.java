package com.ppcex.finance.mq.producer;

import com.ppcex.finance.mq.message.AssetChangeMessage;
import com.ppcex.finance.mq.message.RiskAlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void sendAssetChangeMessage(AssetChangeMessage message) {
        try {
            // 设置消息头
            Message<AssetChangeMessage> mqMessage = MessageBuilder
                    .withPayload(message)
                    .setHeader("message_id", message.getMessageId() != null ? message.getMessageId() : UUID.randomUUID().toString())
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("keys", message.getFlowNo())
                    .setHeader("trace_id", message.getTraceId())
                    .build();

            // 发送事务消息，确保资产变动的一致性
            rocketMQTemplate.sendMessageInTransaction(
                    "asset-topic:ASSET_CHANGE",
                    mqMessage,
                    message.getFlowNo()
            );

            log.info("资产变动消息发送成功: flowNo={}, userId={}, amount={}",
                    message.getFlowNo(), message.getUserId(), message.getAmount());
        } catch (Exception e) {
            log.error("发送资产变动消息失败: flowNo={}", message.getFlowNo(), e);
            throw new RuntimeException("发送资产变动消息失败", e);
        }
    }

    public void sendRiskAlertMessage(RiskAlertMessage message) {
        try {
            // 设置消息头
            Message<RiskAlertMessage> mqMessage = MessageBuilder
                    .withPayload(message)
                    .setHeader("message_id", message.getMessageId() != null ? message.getMessageId() : UUID.randomUUID().toString())
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("keys", message.getEventNo())
                    .setHeader("trace_id", message.getTraceId())
                    .build();

            // 发送广播消息，所有风控消费者都能收到
            rocketMQTemplate.syncSend(
                    "risk-topic:RISK_ALERT",
                    mqMessage
            );

            log.info("风控告警消息发送成功: eventNo={}, userId={}, riskLevel={}",
                    message.getEventNo(), message.getUserId(), message.getRiskLevel());
        } catch (Exception e) {
            log.error("发送风控告警消息失败: eventNo={}", message.getEventNo(), e);
            throw new RuntimeException("发送风控告警消息失败", e);
        }
    }

    public void sendFinancialReportMessage(String reportDate, String reportData) {
        try {
            Message<String> message = MessageBuilder
                    .withPayload(reportData)
                    .setHeader("message_id", UUID.randomUUID().toString())
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("keys", "report_" + reportDate)
                    .build();

            rocketMQTemplate.syncSend(
                    "finance-topic:DAILY_REPORT",
                    message
            );

            log.info("财务报表消息发送成功: reportDate={}", reportDate);
        } catch (Exception e) {
            log.error("发送财务报表消息失败: reportDate={}", reportDate, e);
            throw new RuntimeException("发送财务报表消息失败", e);
        }
    }
}