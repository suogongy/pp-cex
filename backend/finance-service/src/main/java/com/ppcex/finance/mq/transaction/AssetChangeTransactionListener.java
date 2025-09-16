package com.ppcex.finance.mq.transaction;

import com.ppcex.finance.dto.FinancialFlowDTO;
import com.ppcex.finance.entity.FinancialFlow;
import com.ppcex.finance.mq.message.AssetChangeMessage;
import com.ppcex.finance.service.FinancialFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

@Slf4j
@RocketMQTransactionListener
@RequiredArgsConstructor
public class AssetChangeTransactionListener implements RocketMQLocalTransactionListener {

    private final FinancialFlowService financialFlowService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            log.info("执行资产变动本地事务: keys={}", msg.getHeaders().get("keys"));

            // 解析消息体
            AssetChangeMessage assetMessage = (AssetChangeMessage) msg.getPayload();

            // 验证消息
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

            // 执行本地事务 - 创建资金流水
            FinancialFlow flow = financialFlowService.createFinancialFlow(flowDTO);

            log.info("资产变动本地事务执行成功: flowNo={}, flowId={}",
                    assetMessage.getFlowNo(), flow.getId());

            return RocketMQLocalTransactionState.COMMIT;

        } catch (Exception e) {
            log.error("资产变动本地事务执行失败", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        try {
            String flowNo = (String) msg.getHeaders().get("keys");

            log.info("检查资产变动本地事务状态: flowNo={}", flowNo);

            // 检查资金流水是否存在
            FinancialFlow flow = financialFlowService.getUserRecentFlows(
                    extractUserId(msg), 1).stream()
                    .filter(f -> f.getFlowNo().equals(flowNo))
                    .findFirst()
                    .orElse(null);

            if (flow != null) {
                log.info("资产变动本地事务检查通过: flowNo={}", flowNo);
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                log.warn("资产变动本地事务状态未知: flowNo={}", flowNo);
                return RocketMQLocalTransactionState.UNKNOWN;
            }

        } catch (Exception e) {
            log.error("检查资产变动本地事务状态失败", e);
            return RocketMQLocalTransactionState.UNKNOWN;
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

    private Long extractUserId(Message msg) {
        AssetChangeMessage message = (AssetChangeMessage) msg.getPayload();
        return message.getUserId();
    }
}