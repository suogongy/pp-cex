package com.ppcex.match.engine;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import com.ppcex.match.entity.MatchOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderEventHandler implements EventHandler<OrderEvent>, WorkHandler<OrderEvent> {

    private final MatchingEngine matchingEngine;

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        processEvent(event);
    }

    @Override
    public void onEvent(OrderEvent event) {
        processEvent(event);
    }

    private void processEvent(OrderEvent event) {
        try {
            MatchOrder order = event.getOrder();
            if (order == null) {
                log.warn("收到空订单事件");
                return;
            }

            switch (event.getType()) {
                case NEW_ORDER:
                    matchingEngine.processOrder(order);
                    log.debug("处理新订单: {}", order.getOrderNo());
                    break;
                case CANCEL_ORDER:
                    matchingEngine.cancelOrder(order);
                    log.debug("处理取消订单: {}", order.getOrderNo());
                    break;
                case MODIFY_ORDER:
                    matchingEngine.cancelOrder(order);
                    matchingEngine.processOrder(order);
                    log.debug("处理修改订单: {}", order.getOrderNo());
                    break;
                default:
                    log.warn("未知的订单事件类型: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("处理订单事件失败", e);
        }
    }
}