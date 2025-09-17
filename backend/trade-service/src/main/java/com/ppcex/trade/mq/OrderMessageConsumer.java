package com.ppcex.trade.mq;

import com.ppcex.trade.entity.TradeOrder;
import com.ppcex.trade.service.OrderService;
import com.ppcex.common.exception.BusinessException;
import com.ppcex.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Bean;
import java.util.function.Consumer;

@Service
@Slf4j
public class OrderMessageConsumer {

    @Autowired
    private OrderService orderService;

    @Bean
    public Consumer<Message<String>> orderTopic() {
        return this::handleOrderMessage;
    }

    private void handleOrderMessage(Message<String> message) {
        try {
            String payload = message.getPayload();
            String tags = (String) message.getHeaders().get("tags");

            log.info("收到订单消息: tags={}, payload={}", tags, payload);

            switch (tags) {
                case "ORDER_CREATE":
                    handleOrderCreate(payload);
                    break;
                case "ORDER_TRADE":
                    handleOrderTrade(payload);
                    break;
                case "ORDER_CANCEL":
                    handleOrderCancel(payload);
                    break;
                case "ORDER_TIMEOUT":
                    handleOrderTimeout(payload);
                    break;
                default:
                    log.warn("未知的订单消息类型: {}", tags);
            }
        } catch (Exception e) {
            log.error("处理订单消息失败", e);
            throw new RuntimeException("消息处理失败", e);
        }
    }

    private void handleOrderCreate(String payload) {
        try {
            java.util.Map<String, Object> orderData = JsonUtil.parseObject(payload, java.util.Map.class);
            String orderNo = (String) orderData.get("orderNo");
            Long userId = Long.valueOf(orderData.get("userId").toString());
            String symbol = (String) orderData.get("symbol");

            log.info("处理订单创建消息: orderNo={}, userId={}, symbol={}", orderNo, userId, symbol);

            TradeOrder existingOrder = orderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TradeOrder>()
                    .eq("order_no", orderNo));
            if (existingOrder != null) {
                log.warn("订单已存在，跳过处理: orderNo={}", orderNo);
                return;
            }

            TradeOrder order = new TradeOrder();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setSymbol(symbol);
            order.setOrderType(Integer.valueOf(orderData.get("orderType").toString()));
            order.setDirection(Integer.valueOf(orderData.get("direction").toString()));
            order.setPrice(new java.math.BigDecimal(orderData.get("price").toString()));
            order.setAmount(new java.math.BigDecimal(orderData.get("amount").toString()));
            order.setStatus(1);
            order.setExecutedAmount(java.math.BigDecimal.ZERO);
            order.setExecutedValue(java.math.BigDecimal.ZERO);
            order.setFee(java.math.BigDecimal.ZERO);
            order.setCreateTime(java.time.LocalDateTime.now());
            order.setUpdateTime(java.time.LocalDateTime.now());

            orderService.save(order);
            log.info("订单创建成功: orderNo={}", orderNo);

        } catch (Exception e) {
            log.error("处理订单创建消息失败", e);
            throw new BusinessException("订单创建失败");
        }
    }

    private void handleOrderTrade(String payload) {
        try {
            java.util.Map<String, Object> tradeData = JsonUtil.parseObject(payload, java.util.Map.class);
            String orderNo = (String) tradeData.get("orderNo");
            String tradeNo = (String) tradeData.get("tradeNo");
            java.math.BigDecimal tradeAmount = new java.math.BigDecimal(tradeData.get("tradeAmount").toString());
            java.math.BigDecimal tradePrice = new java.math.BigDecimal(tradeData.get("tradePrice").toString());
            java.math.BigDecimal tradeValue = tradeAmount.multiply(tradePrice);

            log.info("处理订单成交消息: orderNo={}, tradeNo={}, amount={}, price={}",
                    orderNo, tradeNo, tradeAmount, tradePrice);

            TradeOrder order = orderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TradeOrder>()
                    .eq("order_no", orderNo));
            if (order == null) {
                log.error("订单不存在: orderNo={}", orderNo);
                return;
            }

            java.math.BigDecimal feeRate = new java.math.BigDecimal("0.001");
            java.math.BigDecimal fee = tradeValue.multiply(feeRate);

            order.setExecutedAmount(order.getExecutedAmount().add(tradeAmount));
            order.setExecutedValue(order.getExecutedValue().add(tradeValue));
            order.setFee(order.getFee().add(fee));
            order.setUpdateTime(java.time.LocalDateTime.now());

            if (order.getExecutedAmount().compareTo(order.getAmount()) >= 0) {
                order.setStatus(3);
            } else {
                order.setStatus(2);
            }

            orderService.updateById(order);
            log.info("订单成交更新成功: orderNo={}, status={}", orderNo, order.getStatus());

        } catch (Exception e) {
            log.error("处理订单成交消息失败", e);
            throw new BusinessException("订单成交处理失败");
        }
    }

    private void handleOrderCancel(String payload) {
        try {
            java.util.Map<String, Object> orderData = JsonUtil.parseObject(payload, java.util.Map.class);
            String orderNo = (String) orderData.get("orderNo");

            log.info("处理订单取消消息: orderNo={}", orderNo);

            TradeOrder order = orderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TradeOrder>()
                    .eq("order_no", orderNo));
            if (order == null) {
                log.warn("订单不存在，跳过处理: orderNo={}", orderNo);
                return;
            }

            if (order.getStatus() == 4) {
                log.warn("订单已取消，跳过处理: orderNo={}", orderNo);
                return;
            }

            order.setStatus(4);
            order.setCancelTime(java.time.LocalDateTime.now());
            order.setUpdateTime(java.time.LocalDateTime.now());

            orderService.updateById(order);
            log.info("订单取消成功: orderNo={}", orderNo);

        } catch (Exception e) {
            log.error("处理订单取消消息失败", e);
            throw new BusinessException("订单取消失败");
        }
    }

    private void handleOrderTimeout(String payload) {
        try {
            java.util.Map<String, Object> timeoutData = JsonUtil.parseObject(payload, java.util.Map.class);
            String orderNo = (String) timeoutData.get("orderNo");

            log.info("处理订单超时消息: orderNo={}", orderNo);

            TradeOrder order = orderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TradeOrder>()
                    .eq("order_no", orderNo));
            if (order == null) {
                log.warn("订单不存在，跳过处理: orderNo={}", orderNo);
                return;
            }

            if (order.getStatus() != 1) {
                log.warn("订单状态不是待成交，跳过处理: orderNo={}, status={}", orderNo, order.getStatus());
                return;
            }

            order.setStatus(4);
            order.setCancelTime(java.time.LocalDateTime.now());
            order.setUpdateTime(java.time.LocalDateTime.now());

            orderService.updateById(order);
            log.info("订单超时取消成功: orderNo={}", orderNo);

        } catch (Exception e) {
            log.error("处理订单超时消息失败", e);
            throw new BusinessException("订单超时处理失败");
        }
    }
}