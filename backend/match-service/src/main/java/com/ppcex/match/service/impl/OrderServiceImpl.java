package com.ppcex.match.service.impl;

import com.ppcex.common.util.SnowflakeIdUtil;
import com.ppcex.match.engine.DisruptorOrderProcessor;
import com.ppcex.match.engine.MatchingEngine;
import com.ppcex.match.engine.OrderEvent;
import com.ppcex.match.entity.MatchOrder;
import com.ppcex.match.enums.OrderStatusEnum;
import com.ppcex.match.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private DisruptorOrderProcessor disruptorOrderProcessor;

    @Autowired
    private MatchingEngine matchingEngine;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_CACHE_PREFIX = "match:order:";
    private static final String USER_ORDERS_PREFIX = "match:user:orders:";

    @Override
    public void processOrder(MatchOrder order) {
        try {
            if (order.getId() == null) {
                order.setId(SnowflakeIdUtil.nextId());
            }
            if (order.getOrderNo() == null) {
                order.setOrderNo(generateOrderNo());
            }
            if (order.getCreateTime() == null) {
                order.setCreateTime(LocalDateTime.now());
            }
            if (order.getStatus() == null) {
                order.setStatus(OrderStatusEnum.PENDING);
            }

            disruptorOrderProcessor.publishOrderEvent(order, OrderEvent.OrderEventType.NEW_ORDER);

            cacheOrder(order);
            log.info("订单已提交处理: {}", order.getOrderNo());
        } catch (Exception e) {
            log.error("处理订单失败: {}", order.getOrderNo(), e);
            throw new RuntimeException("订单处理失败", e);
        }
    }

    @Override
    public void cancelOrder(String orderNo) {
        try {
            MatchOrder order = getOrder(orderNo);
            if (order != null && order.isActive()) {
                disruptorOrderProcessor.publishOrderEvent(order, OrderEvent.OrderEventType.CANCEL_ORDER);
                removeFromCache(orderNo);
                log.info("订单取消请求已提交: {}", orderNo);
            } else {
                log.warn("订单不可取消或不存在: {}", orderNo);
            }
        } catch (Exception e) {
            log.error("取消订单失败: {}", orderNo, e);
            throw new RuntimeException("取消订单失败", e);
        }
    }

    @Override
    public MatchOrder getOrder(String orderNo) {
        try {
            String cacheKey = ORDER_CACHE_PREFIX + orderNo;
            MatchOrder order = (MatchOrder) redisTemplate.opsForValue().get(cacheKey);
            if (order == null) {
                order = loadOrderFromDatabase(orderNo);
                if (order != null) {
                    cacheOrder(order);
                }
            }
            return order;
        } catch (Exception e) {
            log.error("获取订单失败: {}", orderNo, e);
            return null;
        }
    }

    @Override
    public List<MatchOrder> getUserOrders(Long userId) {
        try {
            String cacheKey = USER_ORDERS_PREFIX + userId;
            return (List<MatchOrder>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.error("获取用户订单失败: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public List<MatchOrder> getSymbolOrders(String symbol) {
        try {
            matchingEngine.getOrderBook(symbol);
            return List.of();
        } catch (Exception e) {
            log.error("获取交易对订单失败: {}", symbol, e);
            return List.of();
        }
    }

    private void cacheOrder(MatchOrder order) {
        try {
            String orderKey = ORDER_CACHE_PREFIX + order.getOrderNo();
            String userOrdersKey = USER_ORDERS_PREFIX + order.getUserId();

            redisTemplate.opsForValue().set(orderKey, order, 1, TimeUnit.HOURS);

            redisTemplate.opsForList().rightPush(userOrdersKey, order);
            redisTemplate.expire(userOrdersKey, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("缓存订单失败: {}", order.getOrderNo(), e);
        }
    }

    private void removeFromCache(String orderNo) {
        try {
            String orderKey = ORDER_CACHE_PREFIX + orderNo;
            redisTemplate.delete(orderKey);
        } catch (Exception e) {
            log.error("从缓存删除订单失败: {}", orderNo, e);
        }
    }

    private MatchOrder loadOrderFromDatabase(String orderNo) {
        return null;
    }

    private String generateOrderNo() {
        return "O" + System.currentTimeMillis() + String.format("%06d", (int)(Math.random() * 1000000));
    }
}