package com.ppcex.trade.mq;

import com.ppcex.trade.entity.TradeOrder;
import com.ppcex.trade.dto.OrderVO;
import com.ppcex.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderMessageProducer {

    @Autowired
    private org.springframework.cloud.stream.function.StreamBridge streamBridge;

    public void sendOrderCreateMessage(TradeOrder order) {
        try {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);

            Message<String> message = MessageBuilder
                    .withPayload(JsonUtil.toJsonString(orderVO))
                    .setHeader("message_id", java.util.UUID.randomUUID().toString())
                    .setHeader("keys", order.getOrderNo())
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("tags", "ORDER_CREATE")
                    .build();

            streamBridge.send("order-topic", message);
            log.info("发送订单创建消息成功: orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            log.error("发送订单创建消息失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    public void sendOrderTradeMessage(TradeOrder order, String tradeNo, java.math.BigDecimal tradeAmount, java.math.BigDecimal tradePrice) {
        try {
            java.util.Map<String, Object> tradeData = new java.util.HashMap<>();
            tradeData.put("orderId", order.getId());
            tradeData.put("orderNo", order.getOrderNo());
            tradeData.put("userId", order.getUserId());
            tradeData.put("symbol", order.getSymbol());
            tradeData.put("tradeNo", tradeNo);
            tradeData.put("tradeAmount", tradeAmount);
            tradeData.put("tradePrice", tradePrice);
            tradeData.put("direction", order.getDirection());
            tradeData.put("orderType", order.getOrderType());

            Message<String> message = MessageBuilder
                    .withPayload(JsonUtil.toJsonString(tradeData))
                    .setHeader("message_id", java.util.UUID.randomUUID().toString())
                    .setHeader("keys", order.getOrderNo())
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("tags", "ORDER_TRADE")
                    .build();

            streamBridge.send("order-topic", message);
            log.info("发送订单成交消息成功: orderNo={}, tradeNo={}", order.getOrderNo(), tradeNo);
        } catch (Exception e) {
            log.error("发送订单成交消息失败: orderNo={}, tradeNo={}", order.getOrderNo(), tradeNo, e);
        }
    }

    public void sendOrderCancelMessage(TradeOrder order) {
        try {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);

            Message<String> message = MessageBuilder
                    .withPayload(JsonUtil.toJsonString(orderVO))
                    .setHeader("message_id", java.util.UUID.randomUUID().toString())
                    .setHeader("keys", order.getOrderNo())
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("tags", "ORDER_CANCEL")
                    .build();

            streamBridge.send("order-topic", message);
            log.info("发送订单取消消息成功: orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            log.error("发送订单取消消息失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    public void sendOrderTimeoutMessage(String orderNo, String symbol, Long userId) {
        try {
            java.util.Map<String, Object> timeoutData = new java.util.HashMap<>();
            timeoutData.put("orderNo", orderNo);
            timeoutData.put("symbol", symbol);
            timeoutData.put("userId", userId);

            Message<String> message = MessageBuilder
                    .withPayload(JsonUtil.toJsonString(timeoutData))
                    .setHeader("message_id", java.util.UUID.randomUUID().toString())
                    .setHeader("keys", orderNo)
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("tags", "ORDER_TIMEOUT")
                    .build();

            streamBridge.send("order-topic", message);
            log.info("发送订单超时消息成功: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("发送订单超时消息失败: orderNo={}", orderNo, e);
        }
    }
}