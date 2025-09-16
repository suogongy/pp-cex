package com.ppcex.finance.mq.consumer;

import com.ppcex.finance.dto.FinancialFlowDTO;
import com.ppcex.finance.entity.FinancialFlow;
import com.ppcex.finance.mq.message.AssetChangeMessage;
import com.ppcex.finance.service.FinancialFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RocketMQMessageListener(
        topic = "asset-topic",
        selectorExpression = "ASSET_CHANGE",
        consumerGroup = "finance-asset-consumer",
        messageModel = org.apache.rocketmq.common.protocol.heartbeat.MessageModel.CLUSTERING,
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY
)
public class AssetChangeConsumer implements RocketMQListener<Message<AssetChangeMessage>> {

    private final FinancialFlowService financialFlowService;

    public AssetChangeConsumer(FinancialFlowService financialFlowService) {
        this.financialFlowService = financialFlowService;
    }

    @Override
    public void onMessage(Message<AssetChangeMessage> message) {
        try {
            AssetChangeMessage assetMessage = message.getPayload();

            log.info("处理资产变动消息: flowNo={}, userId={}, amount={}",
                    assetMessage.getFlowNo(), assetMessage.getUserId(), assetMessage.getAmount());

            // 验证消息完整性
            validateAssetChangeMessage(assetMessage);

            // 构建资金流水DTO
            FinancialFlowDTO flowDTO = new FinancialFlowDTO()
                    .setFlowNo(assetMessage.getFlowNo())
                    .setUserId(assetMessage.getUserId())
                    .setCoinId(assetMessage.getCoinId())
                    .setCoinName(assetMessage.getCoinName())
                    .setBusinessType(assetMessage.getBusinessType())
                    .setAmount(assetMessage.getAmount())
                    .setBalanceBefore(assetMessage.getBalanceBefore())
                    .setBalanceAfter(assetMessage.getBalanceAfter())
                    .setFee(assetMessage.getFee())
                    .setStatus(FinancialFlow.Status.SUCCESS.getCode())
                    .setRemark(assetMessage.getRemark())
                    .setRefOrderNo(assetMessage.getRefOrderNo())
                    .setRefTxHash(assetMessage.getRefTxHash());

            // 创建资金流水记录
            FinancialFlow flow = financialFlowService.createFinancialFlow(flowDTO);

            log.info("资产变动消息处理成功: flowNo={}, flowId={}",
                    assetMessage.getFlowNo(), flow.getId());

        } catch (Exception e) {
            log.error("处理资产变动消息失败", e);
            // 消费失败时，RocketMQ会自动重试
            throw new RuntimeException("资产变动消息处理失败", e);
        }
    }

    private void validateAssetChangeMessage(AssetChangeMessage message) {
        if (message.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (message.getCoinId() == null || message.getCoinId().trim().isEmpty()) {
            throw new IllegalArgumentException("币种ID不能为空");
        }
        if (message.getAmount() == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (message.getBusinessType() == null) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        if (message.getBalanceBefore() == null || message.getBalanceAfter() == null) {
            throw new IllegalArgumentException("余额信息不能为空");
        }

        // 验证余额变动计算
        BigDecimal expectedBalance = message.getBalanceBefore()
                .add(message.getAmount())
                .subtract(message.getFee() != null ? message.getFee() : BigDecimal.ZERO);

        if (expectedBalance.compareTo(message.getBalanceAfter()) != 0) {
            throw new IllegalArgumentException(String.format(
                    "余额变动计算错误: 期望=%s, 实际=%s",
                    expectedBalance, message.getBalanceAfter()
            ));
        }
    }
}