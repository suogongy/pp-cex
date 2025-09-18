package com.ppcex.match.mq;

import com.ppcex.match.dto.OrderMessage;
import com.ppcex.match.entity.MatchOrder;
import com.ppcex.match.enums.DirectionEnum;
import com.ppcex.match.enums.OrderStatusEnum;
import com.ppcex.match.enums.OrderTypeEnum;
import com.ppcex.match.service.OrderService;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMessageConsumer {

    private final OrderService orderService;

    @Async
    public void handleOrderMessage(String message) {
        try {
            OrderMessage orderMessage = JSON.parseObject(message, OrderMessage.class);

            switch (orderMessage.getAction()) {
                case "CREATE":
                    handleCreateOrder(orderMessage);
                    break;
                case "CANCEL":
                    handleCancelOrder(orderMessage);
                    break;
                default:
                    log.warn("未知的订单操作: {}", orderMessage.getAction());
            }
        } catch (Exception e) {
            log.error("处理订单消息失败: {}", message, e);
        }
    }

    private void handleCreateOrder(OrderMessage orderMessage) {
        try {
            MatchOrder order = new MatchOrder();
            order.setOrderNo(orderMessage.getOrderNo());
            order.setUserId(orderMessage.getUserId());
            order.setSymbol(orderMessage.getSymbol());
            order.setOrderType(OrderTypeEnum.valueOf(orderMessage.getOrderType()));
            order.setDirection(DirectionEnum.valueOf(orderMessage.getDirection()));
            order.setPrice(new BigDecimal(orderMessage.getPrice()));
            order.setAmount(new BigDecimal(orderMessage.getAmount()));
            order.setExecutedAmount(BigDecimal.ZERO);
            order.setExecutedValue(BigDecimal.ZERO);
            order.setFee(BigDecimal.ZERO);
            order.setStatus(OrderStatusEnum.PENDING);
            order.setTimeInForce(orderMessage.getTimeInForce());
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            orderService.processOrder(order);
            log.info("创建订单消息处理完成: {}", orderMessage.getOrderNo());
        } catch (Exception e) {
            log.error("处理创建订单消息失败: {}", orderMessage.getOrderNo(), e);
        }
    }

    private void handleCancelOrder(OrderMessage orderMessage) {
        try {
            orderService.cancelOrder(orderMessage.getOrderNo());
            log.info("取消订单消息处理完成: {}", orderMessage.getOrderNo());
        } catch (Exception e) {
            log.error("处理取消订单消息失败: {}", orderMessage.getOrderNo(), e);
        }
    }
}