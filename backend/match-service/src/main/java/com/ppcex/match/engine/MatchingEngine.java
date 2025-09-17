package com.ppcex.match.engine;

import com.ppcex.match.entity.MatchOrder;
import com.ppcex.match.entity.TradeRecord;
import com.ppcex.match.enums.DirectionEnum;
import com.ppcex.match.enums.OrderStatusEnum;
import com.ppcex.match.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEngine {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final AtomicLong tradeSequence = new AtomicLong(0);
    private final String ORDER_BOOK_PREFIX = "match:orderbook:";
    private final String TRADE_PREFIX = "match:trade:";

    public void initializeOrderBook(String symbol) {
        orderBooks.computeIfAbsent(symbol, OrderBook::new);
        log.info("初始化订单簿: {}", symbol);
    }

    public void processOrder(MatchOrder order) {
        try {
            OrderBook orderBook = orderBooks.computeIfAbsent(order.getSymbol(), OrderBook::new);

            if (order.getDirection() == DirectionEnum.BUY) {
                processBuyOrder(orderBook, order);
            } else {
                processSellOrder(orderBook, order);
            }

            updateOrderBookToRedis(orderBook);
            broadcastOrderBookUpdate(orderBook);
        } catch (Exception e) {
            log.error("处理订单失败: {}", order.getOrderNo(), e);
            throw new RuntimeException("订单处理失败", e);
        }
    }

    private void processBuyOrder(OrderBook orderBook, MatchOrder buyOrder) {
        BigDecimal remainingAmount = buyOrder.getRemainingAmount();

        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            MatchOrder bestSellOrder = orderBook.getBestSellOrder();
            if (bestSellOrder == null || buyOrder.getPrice().compareTo(bestSellOrder.getPrice()) < 0) {
                break;
            }

            BigDecimal tradeAmount = remainingAmount.min(bestSellOrder.getRemainingAmount());
            BigDecimal tradePrice = bestSellOrder.getPrice();

            executeTrade(orderBook, buyOrder, bestSellOrder, tradeAmount, tradePrice);
            remainingAmount = remainingAmount.subtract(tradeAmount);
        }

        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            orderBook.addBuyOrder(buyOrder);
            buyOrder.setStatus(OrderStatusEnum.PENDING);
        } else {
            buyOrder.setStatus(OrderStatusEnum.FULLY_FILLED);
        }
    }

    private void processSellOrder(OrderBook orderBook, MatchOrder sellOrder) {
        BigDecimal remainingAmount = sellOrder.getRemainingAmount();

        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            MatchOrder bestBuyOrder = orderBook.getBestBuyOrder();
            if (bestBuyOrder == null || sellOrder.getPrice().compareTo(bestBuyOrder.getPrice()) > 0) {
                break;
            }

            BigDecimal tradeAmount = remainingAmount.min(bestBuyOrder.getRemainingAmount());
            BigDecimal tradePrice = bestBuyOrder.getPrice();

            executeTrade(orderBook, bestBuyOrder, sellOrder, tradeAmount, tradePrice);
            remainingAmount = remainingAmount.subtract(tradeAmount);
        }

        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            orderBook.addSellOrder(sellOrder);
            sellOrder.setStatus(OrderStatusEnum.PENDING);
        } else {
            sellOrder.setStatus(OrderStatusEnum.FULLY_FILLED);
        }
    }

    private void executeTrade(OrderBook orderBook, MatchOrder buyOrder, MatchOrder sellOrder,
                             BigDecimal amount, BigDecimal price) {
        BigDecimal value = amount.multiply(price);
        BigDecimal feeRate = new BigDecimal("0.001");
        BigDecimal makerFee = value.multiply(feeRate).setScale(8, RoundingMode.HALF_UP);
        BigDecimal takerFee = value.multiply(feeRate).setScale(8, RoundingMode.HALF_UP);

        TradeRecord tradeRecord = new TradeRecord()
                .setTradeNo(generateTradeNo())
                .setSymbol(orderBook.getSymbol())
                .setMakerOrderId(sellOrder.getId())
                .setTakerOrderId(buyOrder.getId())
                .setMakerUserId(sellOrder.getUserId())
                .setTakerUserId(buyOrder.getUserId())
                .setPrice(price)
                .setAmount(amount)
                .setValue(value)
                .setMakerFee(makerFee)
                .setTakerFee(takerFee)
                .setCreateTime(LocalDateTime.now());

        orderBook.addTrade(tradeRecord);

        updateOrderExecution(buyOrder, amount, value, takerFee);
        updateOrderExecution(sellOrder, amount, value, makerFee);

        tradeService.processTrade(tradeRecord, buyOrder, sellOrder);

        log.info("撮合成功: symbol={}, amount={}, price={}, makerOrderId={}, takerOrderId={}, tradeNo={}",
                orderBook.getSymbol(), amount, price, sellOrder.getId(), buyOrder.getId(), tradeRecord.getTradeNo());
    }

    private void updateOrderExecution(MatchOrder order, BigDecimal amount, BigDecimal value, BigDecimal fee) {
        BigDecimal newExecutedAmount = (order.getExecutedAmount() != null ? order.getExecutedAmount() : BigDecimal.ZERO).add(amount);
        BigDecimal newExecutedValue = (order.getExecutedValue() != null ? order.getExecutedValue() : BigDecimal.ZERO).add(value);
        BigDecimal newFee = (order.getFee() != null ? order.getFee() : BigDecimal.ZERO).add(fee);

        order.setExecutedAmount(newExecutedAmount)
              .setExecutedValue(newExecutedValue)
              .setFee(newFee)
              .setUpdateTime(LocalDateTime.now());

        if (newExecutedAmount.compareTo(order.getAmount()) >= 0) {
            order.setStatus(OrderStatusEnum.FULLY_FILLED);
        } else {
            order.setStatus(OrderStatusEnum.PARTIALLY_FILLED);
        }
    }

    public void cancelOrder(MatchOrder order) {
        OrderBook orderBook = orderBooks.get(order.getSymbol());
        if (orderBook != null) {
            orderBook.removeOrder(order);
            order.setStatus(OrderStatusEnum.CANCELLED)
                  .setUpdateTime(LocalDateTime.now());
            updateOrderBookToRedis(orderBook);
            broadcastOrderBookUpdate(orderBook);
        }
    }

    private String generateTradeNo() {
        return "T" + System.currentTimeMillis() + String.format("%06d", tradeSequence.incrementAndGet() % 1000000);
    }

    private void updateOrderBookToRedis(OrderBook orderBook) {
        String key = ORDER_BOOK_PREFIX + orderBook.getSymbol();
        redisTemplate.opsForValue().set(key, orderBook.getSnapshot(), 1, java.util.concurrent.TimeUnit.MINUTES);
    }

    private void broadcastOrderBookUpdate(OrderBook orderBook) {
        redisTemplate.convertAndSend("orderbook:" + orderBook.getSymbol(), orderBook.getSnapshot());
    }

    public OrderBook getOrderBook(String symbol) {
        return orderBooks.get(symbol);
    }

    public Map<String, Object> getOrderBookSnapshot(String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);
        if (orderBook == null) {
            return Collections.emptyMap();
        }
        return orderBook.getSnapshot();
    }

    public BigDecimal getLatestPrice(String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);
        return orderBook != null ? orderBook.getLatestPrice() : null;
    }

    public Set<String> getActiveSymbols() {
        return new HashSet<>(orderBooks.keySet());
    }

    public void clearOrderBook(String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);
        if (orderBook != null) {
            orderBook.clear();
            updateOrderBookToRedis(orderBook);
        }
    }

    public void clearAllOrderBooks() {
        orderBooks.values().forEach(OrderBook::clear);
        orderBooks.clear();
    }
}