package com.ppcex.match;

import com.ppcex.match.engine.OrderBook;
import com.ppcex.match.entity.MatchOrder;
import com.ppcex.match.entity.TradeRecord;
import com.ppcex.match.enums.DirectionEnum;
import com.ppcex.match.enums.OrderStatusEnum;
import com.ppcex.match.enums.OrderTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineCoreTest {

    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook("BTCUSDT");
    }

    @Test
    void testOrderBookCreation() {
        assertEquals("BTCUSDT", orderBook.getSymbol());
        assertEquals(0, orderBook.getBuyOrderDepths(10).size());
        assertEquals(0, orderBook.getSellOrderDepths(10).size());
        assertEquals(0, orderBook.getRecentTrades().size());
    }

    @Test
    void testAddBuyOrder() {
        MatchOrder buyOrder = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        orderBook.addBuyOrder(buyOrder);

        List<Map<String, Object>> buyDepths = orderBook.getBuyOrderDepths(10);
        assertEquals(1, buyDepths.size());
        assertEquals(new BigDecimal("50000"), buyDepths.get(0).get("price"));
        assertEquals(new BigDecimal("1.0"), buyDepths.get(0).get("amount"));
    }

    @Test
    void testAddSellOrder() {
        MatchOrder sellOrder = createSellOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        orderBook.addSellOrder(sellOrder);

        List<Map<String, Object>> sellDepths = orderBook.getSellOrderDepths(10);
        assertEquals(1, sellDepths.size());
        assertEquals(new BigDecimal("50000"), sellDepths.get(0).get("price"));
        assertEquals(new BigDecimal("1.0"), sellDepths.get(0).get("amount"));
    }

    @Test
    void testGetBestBuyOrder() {
        MatchOrder buyOrder1 = createBuyOrder(new BigDecimal("49900"), new BigDecimal("1.0"));
        MatchOrder buyOrder2 = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        MatchOrder buyOrder3 = createBuyOrder(new BigDecimal("49800"), new BigDecimal("1.0"));

        orderBook.addBuyOrder(buyOrder1);
        orderBook.addBuyOrder(buyOrder2);
        orderBook.addBuyOrder(buyOrder3);

        MatchOrder bestBuyOrder = orderBook.getBestBuyOrder();
        assertNotNull(bestBuyOrder);
        assertEquals(new BigDecimal("50000"), bestBuyOrder.getPrice());
    }

    @Test
    void testGetBestSellOrder() {
        MatchOrder sellOrder1 = createSellOrder(new BigDecimal("50100"), new BigDecimal("1.0"));
        MatchOrder sellOrder2 = createSellOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        MatchOrder sellOrder3 = createSellOrder(new BigDecimal("50200"), new BigDecimal("1.0"));

        orderBook.addSellOrder(sellOrder1);
        orderBook.addSellOrder(sellOrder2);
        orderBook.addSellOrder(sellOrder3);

        MatchOrder bestSellOrder = orderBook.getBestSellOrder();
        assertNotNull(bestSellOrder);
        assertEquals(new BigDecimal("50000"), bestSellOrder.getPrice());
    }

    @Test
    void testPricePriorityBuy() {
        MatchOrder buyOrder1 = createBuyOrder(new BigDecimal("49900"), new BigDecimal("1.0"));
        MatchOrder buyOrder2 = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        MatchOrder buyOrder3 = createBuyOrder(new BigDecimal("49800"), new BigDecimal("1.0"));

        orderBook.addBuyOrder(buyOrder1);
        orderBook.addBuyOrder(buyOrder2);
        orderBook.addBuyOrder(buyOrder3);

        List<Map<String, Object>> buyDepths = orderBook.getBuyOrderDepths(10);
        assertEquals(3, buyDepths.size());
        assertEquals(new BigDecimal("50000"), buyDepths.get(0).get("price"));
        assertEquals(new BigDecimal("49900"), buyDepths.get(1).get("price"));
        assertEquals(new BigDecimal("49800"), buyDepths.get(2).get("price"));
    }

    @Test
    void testPricePrioritySell() {
        MatchOrder sellOrder1 = createSellOrder(new BigDecimal("50100"), new BigDecimal("1.0"));
        MatchOrder sellOrder2 = createSellOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        MatchOrder sellOrder3 = createSellOrder(new BigDecimal("50200"), new BigDecimal("1.0"));

        orderBook.addSellOrder(sellOrder1);
        orderBook.addSellOrder(sellOrder2);
        orderBook.addSellOrder(sellOrder3);

        List<Map<String, Object>> sellDepths = orderBook.getSellOrderDepths(10);
        assertEquals(3, sellDepths.size());
        assertEquals(new BigDecimal("50000"), sellDepths.get(0).get("price"));
        assertEquals(new BigDecimal("50100"), sellDepths.get(1).get("price"));
        assertEquals(new BigDecimal("50200"), sellDepths.get(2).get("price"));
    }

    @Test
    void testSamePriceAggregation() {
        MatchOrder buyOrder1 = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        MatchOrder buyOrder2 = createBuyOrder(new BigDecimal("50000"), new BigDecimal("2.0"));
        MatchOrder buyOrder3 = createBuyOrder(new BigDecimal("50000"), new BigDecimal("0.5"));

        orderBook.addBuyOrder(buyOrder1);
        orderBook.addBuyOrder(buyOrder2);
        orderBook.addBuyOrder(buyOrder3);

        List<Map<String, Object>> buyDepths = orderBook.getBuyOrderDepths(10);
        assertEquals(1, buyDepths.size());
        assertEquals(new BigDecimal("50000"), buyDepths.get(0).get("price"));
        assertEquals(new BigDecimal("3.5"), buyDepths.get(0).get("amount"));
    }

    @Test
    void testRemoveOrder() {
        MatchOrder buyOrder = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        orderBook.addBuyOrder(buyOrder);

        assertEquals(1, orderBook.getBuyOrderDepths(10).size());

        orderBook.removeOrder(buyOrder);

        assertEquals(0, orderBook.getBuyOrderDepths(10).size());
    }

    @Test
    void testAddTrade() {
        TradeRecord tradeRecord = createTradeRecord(new BigDecimal("50000"), new BigDecimal("1.0"));
        orderBook.addTrade(tradeRecord);

        assertEquals(1, orderBook.getRecentTrades().size());
        assertEquals(new BigDecimal("50000"), orderBook.getLatestPrice());
        assertEquals(new BigDecimal("1.0"), orderBook.getLatestVolume());
    }

    @Test
    void testTradeRecordLimit() {
        for (int i = 0; i < 1005; i++) {
            TradeRecord tradeRecord = createTradeRecord(new BigDecimal("50000").add(new BigDecimal(i)), new BigDecimal("1.0"));
            orderBook.addTrade(tradeRecord);
        }

        assertEquals(1000, orderBook.getRecentTrades().size());
        assertEquals(new BigDecimal("51004"), orderBook.getLatestPrice());
    }

    @Test
    void testOrderDepthLimit() {
        for (int i = 0; i < 25; i++) {
            MatchOrder buyOrder = createBuyOrder(new BigDecimal("50000").subtract(new BigDecimal(i * 100)), new BigDecimal("1.0"));
            orderBook.addBuyOrder(buyOrder);
        }

        List<Map<String, Object>> buyDepths = orderBook.getBuyOrderDepths(10);
        assertEquals(10, buyDepths.size());
        assertEquals(new BigDecimal("50000"), buyDepths.get(0).get("price"));

        List<Map<String, Object>> buyDepths20 = orderBook.getBuyOrderDepths(20);
        assertEquals(20, buyDepths20.size());

        List<Map<String, Object>> buyDepthsAll = orderBook.getBuyOrderDepths(100);
        assertEquals(25, buyDepthsAll.size());
    }

    @Test
    void testOrderBookSnapshot() {
        MatchOrder buyOrder = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        MatchOrder sellOrder = createSellOrder(new BigDecimal("50100"), new BigDecimal("1.0"));
        TradeRecord tradeRecord = createTradeRecord(new BigDecimal("50000"), new BigDecimal("0.5"));

        orderBook.addBuyOrder(buyOrder);
        orderBook.addSellOrder(sellOrder);
        orderBook.addTrade(tradeRecord);

        Map<String, Object> snapshot = orderBook.getSnapshot();
        assertNotNull(snapshot);
        assertEquals("BTCUSDT", snapshot.get("symbol"));
        assertEquals(3L, snapshot.get("sequence"));
        assertEquals(new BigDecimal("50000"), snapshot.get("latestPrice"));
        assertEquals(new BigDecimal("0.5"), snapshot.get("latestVolume"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> buyOrders = (List<Map<String, Object>>) snapshot.get("buyOrders");
        assertEquals(1, buyOrders.size());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sellOrders = (List<Map<String, Object>>) snapshot.get("sellOrders");
        assertEquals(1, sellOrders.size());

        @SuppressWarnings("unchecked")
        List<TradeRecord> recentTrades = (List<TradeRecord>) snapshot.get("recentTrades");
        assertEquals(1, recentTrades.size());
    }

    @Test
    void testClearOrderBook() {
        MatchOrder buyOrder = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        MatchOrder sellOrder = createSellOrder(new BigDecimal("50100"), new BigDecimal("1.0"));
        TradeRecord tradeRecord = createTradeRecord(new BigDecimal("50000"), new BigDecimal("0.5"));

        orderBook.addBuyOrder(buyOrder);
        orderBook.addSellOrder(sellOrder);
        orderBook.addTrade(tradeRecord);

        assertEquals(1, orderBook.getBuyOrderDepths(10).size());
        assertEquals(1, orderBook.getSellOrderDepths(10).size());
        assertEquals(1, orderBook.getRecentTrades().size());
        assertEquals(3, orderBook.getSequence());

        orderBook.clear();

        assertEquals(0, orderBook.getBuyOrderDepths(10).size());
        assertEquals(0, orderBook.getSellOrderDepths(10).size());
        assertEquals(0, orderBook.getRecentTrades().size());
        assertEquals(0, orderBook.getSequence());
        // Latest price and volume are not cleared in current implementation
    }

    @Test
    void testOrderStatus() {
        MatchOrder order = createBuyOrder(new BigDecimal("50000"), new BigDecimal("1.0"));
        assertEquals(OrderStatusEnum.PENDING, order.getStatus());
        assertTrue(order.isActive());
        assertFalse(order.isFullyFilled());
        assertEquals(new BigDecimal("1.0"), order.getRemainingAmount());

        order.setExecutedAmount(new BigDecimal("0.5"));
        assertEquals(new BigDecimal("0.5"), order.getRemainingAmount());

        order.setExecutedAmount(new BigDecimal("1.0"));
        assertEquals(new BigDecimal("0.0"), order.getRemainingAmount());
    }

    @Test
    void testPerformance() {
        int orderCount = 100;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < orderCount; i++) {
            MatchOrder order = createBuyOrder(new BigDecimal("50000").add(new BigDecimal(i)), new BigDecimal("0.1"));
            orderBook.addBuyOrder(order);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("添加 " + orderCount + " 个买单耗时: " + duration + "ms");
        System.out.println("平均每单耗时: " + (double) duration / orderCount + "ms");

        assertTrue(duration < 1000, "添加100个订单超过1秒");
    }

    private MatchOrder createBuyOrder(BigDecimal price, BigDecimal amount) {
        return createOrder(DirectionEnum.BUY, price, amount);
    }

    private MatchOrder createSellOrder(BigDecimal price, BigDecimal amount) {
        return createOrder(DirectionEnum.SELL, price, amount);
    }

    private MatchOrder createOrder(DirectionEnum direction, BigDecimal price, BigDecimal amount) {
        return new MatchOrder()
                .setId(System.currentTimeMillis())
                .setOrderNo("ORD" + System.currentTimeMillis())
                .setUserId(1001L)
                .setSymbol("BTCUSDT")
                .setOrderType(OrderTypeEnum.LIMIT)
                .setDirection(direction)
                .setPrice(price)
                .setAmount(amount)
                .setExecutedAmount(BigDecimal.ZERO)
                .setExecutedValue(BigDecimal.ZERO)
                .setFee(BigDecimal.ZERO)
                .setStatus(OrderStatusEnum.PENDING)
                .setTimeInForce(1)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());
    }

    private TradeRecord createTradeRecord(BigDecimal price, BigDecimal amount) {
        return new TradeRecord()
                .setTradeNo("T" + System.currentTimeMillis())
                .setSymbol("BTCUSDT")
                .setMakerOrderId(1L)
                .setTakerOrderId(2L)
                .setMakerUserId(1001L)
                .setTakerUserId(1002L)
                .setPrice(price)
                .setAmount(amount)
                .setValue(price.multiply(amount))
                .setMakerFee(BigDecimal.ZERO)
                .setTakerFee(BigDecimal.ZERO)
                .setCreateTime(LocalDateTime.now());
    }
}