package com.ppcex.trade.engine;

import com.ppcex.trade.entity.TradeOrder;
import com.ppcex.trade.entity.TradeDetail;
import com.ppcex.trade.service.OrderService;
import com.ppcex.trade.service.TradePairService;
import com.ppcex.trade.service.TradeDetailService;
import com.ppcex.common.util.SnowflakeIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class MatchingEngine {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TradePairService tradePairService;

    @Autowired
    private TradeDetailService tradeDetailService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final ReentrantLock matchingLock = new ReentrantLock();

    private static final String ORDER_BOOK_PREFIX = "orderbook:";
    private static final String TRADE_PRICE_PREFIX = "trade:price:";

    public void initializeOrderBook(String symbol) {
        if (!orderBooks.containsKey(symbol)) {
            orderBooks.put(symbol, new OrderBook(symbol));
            log.info("初始化订单簿: {}", symbol);
        }
    }

    public void processOrder(TradeOrder order) {
        matchingLock.lock();
        try {
            String symbol = order.getSymbol();
            OrderBook orderBook = orderBooks.computeIfAbsent(symbol, k -> new OrderBook(symbol));

            if (order.getDirection() == 1) {
                processBuyOrder(orderBook, order);
            } else {
                processSellOrder(orderBook, order);
            }

            updateOrderBookToRedis(orderBook);
        } finally {
            matchingLock.unlock();
        }
    }

    private void processBuyOrder(OrderBook orderBook, TradeOrder buyOrder) {
        BigDecimal remainingAmount = buyOrder.getAmount().subtract(buyOrder.getExecutedAmount());

        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && !orderBook.getSellOrders().isEmpty()) {
            TradeOrder bestSellOrder = orderBook.getSellOrders().peek();
            if (bestSellOrder == null || buyOrder.getPrice().compareTo(bestSellOrder.getPrice()) < 0) {
                break;
            }

            BigDecimal tradeAmount = remainingAmount.min(bestSellOrder.getAmount().subtract(bestSellOrder.getExecutedAmount()));
            BigDecimal tradePrice = bestSellOrder.getPrice();

            executeTrade(orderBook, buyOrder, bestSellOrder, tradeAmount, tradePrice);

            remainingAmount = remainingAmount.subtract(tradeAmount);

            if (bestSellOrder.getAmount().subtract(bestSellOrder.getExecutedAmount()).compareTo(BigDecimal.ZERO) <= 0) {
                orderBook.getSellOrders().poll();
            }
        }

        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            orderBook.getBuyOrders().offer(buyOrder);
        }
    }

    private void processSellOrder(OrderBook orderBook, TradeOrder sellOrder) {
        BigDecimal remainingAmount = sellOrder.getAmount().subtract(sellOrder.getExecutedAmount());

        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && !orderBook.getBuyOrders().isEmpty()) {
            TradeOrder bestBuyOrder = orderBook.getBuyOrders().peek();
            if (bestBuyOrder == null || sellOrder.getPrice().compareTo(bestBuyOrder.getPrice()) > 0) {
                break;
            }

            BigDecimal tradeAmount = remainingAmount.min(bestBuyOrder.getAmount().subtract(bestBuyOrder.getExecutedAmount()));
            BigDecimal tradePrice = bestBuyOrder.getPrice();

            executeTrade(orderBook, bestBuyOrder, sellOrder, tradeAmount, tradePrice);

            remainingAmount = remainingAmount.subtract(tradeAmount);

            if (bestBuyOrder.getAmount().subtract(bestBuyOrder.getExecutedAmount()).compareTo(BigDecimal.ZERO) <= 0) {
                orderBook.getBuyOrders().poll();
            }
        }

        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            orderBook.getSellOrders().offer(sellOrder);
        }
    }

    private void executeTrade(OrderBook orderBook, TradeOrder buyOrder, TradeOrder sellOrder,
                             BigDecimal amount, BigDecimal price) {
        BigDecimal value = amount.multiply(price);

        com.ppcex.trade.dto.TradePairVO tradePair = tradePairService.getTradePairBySymbol(orderBook.getSymbol());
        BigDecimal feeRate = tradePair != null ? tradePair.getFeeRate() : new BigDecimal("0.001");

        BigDecimal makerFee = value.multiply(feeRate).setScale(8, RoundingMode.HALF_UP);
        BigDecimal takerFee = value.multiply(feeRate).setScale(8, RoundingMode.HALF_UP);

        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setTradeNo(tradeDetailService.generateTradeNo());
        tradeDetail.setSymbol(orderBook.getSymbol());
        tradeDetail.setMakerOrderId(sellOrder.getId());
        tradeDetail.setTakerOrderId(buyOrder.getId());
        tradeDetail.setMakerUserId(sellOrder.getUserId());
        tradeDetail.setTakerUserId(buyOrder.getUserId());
        tradeDetail.setPrice(price);
        tradeDetail.setAmount(amount);
        tradeDetail.setValue(value);
        tradeDetail.setMakerFee(makerFee);
        tradeDetail.setTakerFee(takerFee);
        tradeDetail.setCreateTime(LocalDateTime.now());

        tradeDetailService.createTradeDetail(tradeDetail);

        orderService.updateOrderExecution(buyOrder.getId(), amount, value, takerFee);
        orderService.updateOrderExecution(sellOrder.getId(), amount, value, makerFee);

        orderBook.addTrade(tradeDetail);
        updateTradePrice(orderBook.getSymbol(), price);

        log.info("撮合成功: symbol={}, amount={}, price={}, makerOrderId={}, takerOrderId={}",
                orderBook.getSymbol(), amount, price, sellOrder.getId(), buyOrder.getId());
    }

    private void updateOrderBookToRedis(OrderBook orderBook) {
        String key = ORDER_BOOK_PREFIX + orderBook.getSymbol();

        Map<String, Object> orderBookData = new HashMap<>();
        orderBookData.put("symbol", orderBook.getSymbol());
        orderBookData.put("timestamp", System.currentTimeMillis());

        List<Map<String, Object>> buyOrders = new ArrayList<>();
        for (TradeOrder order : orderBook.getBuyOrders()) {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("price", order.getPrice());
            orderData.put("amount", order.getAmount().subtract(order.getExecutedAmount()));
            buyOrders.add(orderData);
        }
        orderBookData.put("buyOrders", buyOrders);

        List<Map<String, Object>> sellOrders = new ArrayList<>();
        for (TradeOrder order : orderBook.getSellOrders()) {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("price", order.getPrice());
            orderData.put("amount", order.getAmount().subtract(order.getExecutedAmount()));
            sellOrders.add(orderData);
        }
        orderBookData.put("sellOrders", sellOrders);

        redisTemplate.opsForValue().set(key, orderBookData, 1, java.util.concurrent.TimeUnit.MINUTES);
    }

    private void updateTradePrice(String symbol, BigDecimal price) {
        String key = TRADE_PRICE_PREFIX + symbol;
        redisTemplate.opsForValue().set(key, price.toString(), 5, java.util.concurrent.TimeUnit.MINUTES);
    }

    public Map<String, Object> getOrderBook(String symbol) {
        String key = ORDER_BOOK_PREFIX + symbol;
        return (Map<String, Object>) redisTemplate.opsForValue().get(key);
    }

    public BigDecimal getLatestTradePrice(String symbol) {
        String key = TRADE_PRICE_PREFIX + symbol;
        String priceStr = (String) redisTemplate.opsForValue().get(key);
        return priceStr != null ? new BigDecimal(priceStr) : null;
    }

    public void cancelOrder(TradeOrder order) {
        matchingLock.lock();
        try {
            OrderBook orderBook = orderBooks.get(order.getSymbol());
            if (orderBook != null) {
                if (order.getDirection() == 1) {
                    orderBook.getBuyOrders().remove(order);
                } else {
                    orderBook.getSellOrders().remove(order);
                }
                updateOrderBookToRedis(orderBook);
            }
        } finally {
            matchingLock.unlock();
        }
    }

    public static class OrderBook {
        private final String symbol;
        private final PriorityBlockingQueue<TradeOrder> buyOrders;
        private final PriorityBlockingQueue<TradeOrder> sellOrders;
        private final List<TradeDetail> recentTrades;

        public OrderBook(String symbol) {
            this.symbol = symbol;
            this.buyOrders = new PriorityBlockingQueue<>(1000,
                Comparator.comparing(TradeOrder::getPrice).reversed()
                    .thenComparing(TradeOrder::getCreateTime));
            this.sellOrders = new PriorityBlockingQueue<>(1000,
                Comparator.comparing(TradeOrder::getPrice)
                    .thenComparing(TradeOrder::getCreateTime));
            this.recentTrades = new ArrayList<>();
        }

        public String getSymbol() {
            return symbol;
        }

        public PriorityBlockingQueue<TradeOrder> getBuyOrders() {
            return buyOrders;
        }

        public PriorityBlockingQueue<TradeOrder> getSellOrders() {
            return sellOrders;
        }

        public List<TradeDetail> getRecentTrades() {
            return recentTrades;
        }

        public void addTrade(TradeDetail trade) {
            recentTrades.add(trade);
            if (recentTrades.size() > 100) {
                recentTrades.remove(0);
            }
        }
    }
}