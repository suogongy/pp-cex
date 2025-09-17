package com.ppcex.match.engine;

import com.ppcex.match.entity.MatchOrder;
import com.ppcex.match.entity.TradeRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Data
public class OrderBook {
    private final String symbol;
    private final ConcurrentSkipListMap<BigDecimal, List<MatchOrder>> buyOrders;
    private final ConcurrentSkipListMap<BigDecimal, List<MatchOrder>> sellOrders;
    private final List<TradeRecord> recentTrades;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private BigDecimal latestPrice;
    private BigDecimal latestVolume;
    private long sequence;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyOrders = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        this.sellOrders = new ConcurrentSkipListMap<>();
        this.recentTrades = new CopyOnWriteArrayList<>();
        this.sequence = 0;
    }

    public void addBuyOrder(MatchOrder order) {
        lock.writeLock().lock();
        try {
            buyOrders
                    .computeIfAbsent(order.getPrice(), k -> new ArrayList<>())
                    .add(order);
            sequence++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addSellOrder(MatchOrder order) {
        lock.writeLock().lock();
        try {
            sellOrders.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
            sequence++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeOrder(MatchOrder order) {
        lock.writeLock().lock();
        try {
            if (order.getDirection().getCode() == 1) {
                removeOrderFromPriceLevel(buyOrders, order);
            } else {
                removeOrderFromPriceLevel(sellOrders, order);
            }
            sequence++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeOrderFromPriceLevel(ConcurrentSkipListMap<BigDecimal, List<MatchOrder>> orders,
            MatchOrder order) {
        List<MatchOrder> ordersAtPrice = orders.get(order.getPrice());
        if (ordersAtPrice != null) {
            ordersAtPrice
                    .removeIf(o -> o.getId().equals(order.getId()));
            if (ordersAtPrice.isEmpty()) {
                orders.remove(order.getPrice());
            }
        }
    }

    public void updateOrderAmount(MatchOrder order, BigDecimal newAmount) {
        lock.writeLock().lock();
        try {
            order.setExecutedAmount(order.getAmount().subtract(newAmount));
            sequence++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addTrade(TradeRecord trade) {
        recentTrades.add(trade);
        if (recentTrades.size() > 1000) {
            recentTrades.remove(0);
        }
        this.latestPrice = trade.getPrice();
        this.latestVolume = trade.getAmount();
        sequence++;
    }

    public MatchOrder getBestBuyOrder() {
        lock.readLock().lock();
        try {
            Map.Entry<BigDecimal, List<MatchOrder>> firstEntry = buyOrders.firstEntry();
            if (firstEntry != null && !firstEntry.getValue().isEmpty()) {
                return firstEntry.getValue().get(0);
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public MatchOrder getBestSellOrder() {
        lock.readLock().lock();
        try {
            Map.Entry<BigDecimal, List<MatchOrder>> firstEntry = sellOrders.firstEntry();
            if (firstEntry != null && !firstEntry.getValue().isEmpty()) {
                return firstEntry.getValue().get(0);
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Map<String, Object>> getBuyOrderDepths(int limit) {
        return getOrderDepths(buyOrders, limit);
    }

    public List<Map<String, Object>> getSellOrderDepths(int limit) {
        return getOrderDepths(sellOrders, limit);
    }

    private List<Map<String, Object>> getOrderDepths(ConcurrentSkipListMap<BigDecimal, List<MatchOrder>> orders,
            int limit) {
        List<Map<String, Object>> depths = new ArrayList<>();
        lock.readLock().lock();
        try {
            int count = 0;
            for (Map.Entry<BigDecimal, List<MatchOrder>> entry : orders.entrySet()) {
                if (count >= limit)
                    break;

                BigDecimal totalAmount = BigDecimal.ZERO;
                for (MatchOrder order : entry.getValue()) {
                    totalAmount = totalAmount.add(order.getRemainingAmount());
                }

                Map<String, Object> depth = new HashMap<>();
                depth.put("price", entry.getKey());
                depth.put("amount", totalAmount);
                depth.put("total", totalAmount);
                depths.add(depth);
                count++;
            }
        } finally {
            lock.readLock().unlock();
        }
        return depths;
    }

    public Map<String, Object> getSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("symbol", symbol);
        snapshot.put("sequence", sequence);
        snapshot.put("latestPrice", latestPrice);
        snapshot.put("latestVolume", latestVolume);
        snapshot.put("buyOrders", getBuyOrderDepths(20));
        snapshot.put("sellOrders", getSellOrderDepths(20));
        snapshot.put("recentTrades", recentTrades.subList(Math.max(0, recentTrades.size() - 50), recentTrades.size()));
        snapshot.put("timestamp", System.currentTimeMillis());
        return snapshot;
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            buyOrders.clear();
            sellOrders.clear();
            recentTrades.clear();
            sequence = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }
}