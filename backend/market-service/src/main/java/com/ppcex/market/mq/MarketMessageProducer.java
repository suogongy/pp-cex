package com.ppcex.market.mq;

import com.alibaba.fastjson2.JSON;
import com.ppcex.market.dto.MarketTickerVO;
import com.ppcex.market.dto.MarketTradeVO;
import com.ppcex.market.dto.MarketDepthVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MarketMessageProducer {

    @Autowired
    private StreamBridge streamBridge;

    private static final String MARKET_TOPIC = "market-topic";
    private static final String TICK_TOPIC = "tick-topic";

    /**
     * 发送行情更新消息
     */
    public void sendTickerUpdate(String symbol, BigDecimal lastPrice, BigDecimal highPrice,
                                BigDecimal lowPrice, BigDecimal volume, BigDecimal quoteVolume, Integer count) {
        try {
            Map<String, Object> message = Map.of(
                    "symbol", symbol,
                    "lastPrice", lastPrice,
                    "highPrice", highPrice,
                    "lowPrice", lowPrice,
                    "volume", volume,
                    "quoteVolume", quoteVolume,
                    "count", count,
                    "timestamp", System.currentTimeMillis()
            );

            Message<Map<String, Object>> msg = MessageBuilder.withPayload(message)
                    .setHeader("messageType", "TICKER_UPDATE")
                    .setHeader("symbol", symbol)
                    .build();

            streamBridge.send("marketTopic-out-0", msg);
            log.debug("发送行情更新消息: {}", symbol);

        } catch (Exception e) {
            log.error("发送行情更新消息失败: {}", symbol, e);
        }
    }

    /**
     * 发送成交明细消息
     */
    public void sendTradeMessage(String tradeId, String symbol, BigDecimal price, BigDecimal amount,
                                 Long timestamp, Integer isBuyerMaker) {
        try {
            Map<String, Object> message = Map.of(
                    "tradeId", tradeId,
                    "symbol", symbol,
                    "price", price,
                    "amount", amount,
                    "quoteVolume", price.multiply(amount),
                    "timestamp", timestamp != null ? timestamp : System.currentTimeMillis(),
                    "isBuyerMaker", isBuyerMaker,
                    "messageType", "TRADE_UPDATE"
            );

            Message<Map<String, Object>> msg = MessageBuilder.withPayload(message)
                    .setHeader("messageType", "TRADE_UPDATE")
                    .setHeader("symbol", symbol)
                    .build();

            streamBridge.send("marketTopic-out-0", msg);
            log.debug("发送成交明细消息: {} {} {}", symbol, price, amount);

        } catch (Exception e) {
            log.error("发送成交明细消息失败: {}", symbol, e);
        }
    }

    /**
     * 发送深度更新消息
     */
    public void sendDepthUpdate(String symbol, List<BigDecimal[]> bids, List<BigDecimal[]> asks, Long timestamp) {
        try {
            Map<String, Object> message = Map.of(
                    "symbol", symbol,
                    "bids", bids,
                    "asks", asks,
                    "timestamp", timestamp != null ? timestamp : System.currentTimeMillis(),
                    "messageType", "DEPTH_UPDATE"
            );

            Message<Map<String, Object>> msg = MessageBuilder.withPayload(message)
                    .setHeader("messageType", "DEPTH_UPDATE")
                    .setHeader("symbol", symbol)
                    .build();

            streamBridge.send("marketTopic-out-0", msg);
            log.debug("发送深度更新消息: {}", symbol);

        } catch (Exception e) {
            log.error("发送深度更新消息失败: {}", symbol, e);
        }
    }

    /**
     * 发送K线更新消息
     */
    public void sendKlineUpdate(String symbol, String interval, Long openTime, BigDecimal openPrice,
                                BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                                BigDecimal volume, Integer tradesCount) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("symbol", symbol);
            message.put("interval", interval);
            message.put("openTime", openTime);
            message.put("openPrice", openPrice);
            message.put("highPrice", highPrice);
            message.put("lowPrice", lowPrice);
            message.put("closePrice", closePrice);
            message.put("volume", volume);
            message.put("tradesCount", tradesCount);
            message.put("timestamp", System.currentTimeMillis());
            message.put("messageType", "KLINE_UPDATE");

            Message<Map<String, Object>> msg = MessageBuilder.withPayload(message)
                    .setHeader("messageType", "KLINE_UPDATE")
                    .setHeader("symbol", symbol)
                    .build();

            streamBridge.send("tickTopic-out-0", msg);
            log.debug("发送K线更新消息: {} {}", symbol, interval);

        } catch (Exception e) {
            log.error("发送K线更新消息失败: {} {}", symbol, interval, e);
        }
    }

    /**
     * 发送市场统计消息
     */
    public void sendMarketStatistics(List<MarketTickerVO> tickers) {
        try {
            Map<String, Object> message = Map.of(
                    "tickers", tickers,
                    "totalMarketCap", tickers.stream()
                            .mapToLong(ticker -> ticker.getQuoteVolume().longValue())
                            .sum(),
                    "totalVolume24h", tickers.stream()
                            .mapToLong(ticker -> ticker.getVolume().longValue())
                            .sum(),
                    "timestamp", System.currentTimeMillis(),
                    "messageType", "MARKET_STATISTICS"
            );

            Message<Map<String, Object>> msg = MessageBuilder.withPayload(message)
                    .setHeader("messageType", "MARKET_STATISTICS")
                    .build();

            streamBridge.send("tickTopic-out-0", msg);
            log.debug("发送市场统计消息");

        } catch (Exception e) {
            log.error("发送市场统计消息失败", e);
        }
    }

    /**
     * 发送系统状态消息
     */
    public void sendSystemStatus(String status, String message) {
        try {
            Map<String, Object> payload = Map.of(
                    "status", status,
                    "message", message,
                    "timestamp", System.currentTimeMillis(),
                    "service", "market-service"
            );

            Message<Map<String, Object>> msg = MessageBuilder.withPayload(payload)
                    .setHeader("messageType", "SYSTEM_STATUS")
                    .build();

            streamBridge.send("marketTopic-out-0", msg);
            log.info("发送系统状态消息: {}", status);

        } catch (Exception e) {
            log.error("发送系统状态消息失败", e);
        }
    }
}