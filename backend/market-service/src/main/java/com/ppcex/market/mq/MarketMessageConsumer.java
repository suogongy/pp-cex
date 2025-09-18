package com.ppcex.market.mq;

import com.alibaba.fastjson2.JSON;
import com.ppcex.market.service.MarketTickerService;
import com.ppcex.market.service.MarketTradeService;
import com.ppcex.market.service.MarketDepthService;
import com.ppcex.market.service.MarketKlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class MarketMessageConsumer {

    @Autowired
    private MarketTickerService marketTickerService;

    @Autowired
    private MarketTradeService marketTradeService;

    @Autowired
    private MarketDepthService marketDepthService;

    @Autowired
    private MarketKlineService marketKlineService;

    @Autowired
    private MarketMessageProducer marketMessageProducer;

    /**
     * 处理来自trade-service的成交消息
     */
    @Bean
    public Consumer<Message<Map<String, Object>>> tradeTopic() {
        return message -> {
            try {
                Map<String, Object> payload = message.getPayload();
                String messageType = (String) payload.get("messageType");

                log.debug("收到trade消息: {}", messageType);

                if ("TRADE_EXECUTED".equals(messageType)) {
                    processTradeExecution(payload);
                } else if ("ORDER_UPDATED".equals(messageType)) {
                    processOrderUpdate(payload);
                } else if ("PAIR_UPDATED".equals(messageType)) {
                    processPairUpdate(payload);
                }

            } catch (Exception e) {
                log.error("处理trade消息失败", e);
            }
        };
    }

    /**
     * 处理内部market-topic消息
     */
    @Bean
    public Consumer<Message<Map<String, Object>>> marketTopic() {
        return message -> {
            try {
                Map<String, Object> payload = message.getPayload();
                String messageType = (String) payload.get("messageType");

                log.debug("收到market消息: {}", messageType);

                switch (messageType) {
                    case "TICKER_UPDATE":
                        processTickerUpdate(payload);
                        break;
                    case "TRADE_UPDATE":
                        processTradeUpdate(payload);
                        break;
                    case "DEPTH_UPDATE":
                        processDepthUpdate(payload);
                        break;
                    case "SYSTEM_COMMAND":
                        processSystemCommand(payload);
                        break;
                }

            } catch (Exception e) {
                log.error("处理market消息失败", e);
            }
        };
    }

    /**
     * 处理tick-topic消息（K线更新等）
     */
    @Bean
    public Consumer<Message<Map<String, Object>>> tickTopic() {
        return message -> {
            try {
                Map<String, Object> payload = message.getPayload();
                String messageType = (String) payload.get("messageType");

                log.debug("收到tick消息: {}", messageType);

                if ("KLINE_UPDATE".equals(messageType)) {
                    processKlineUpdate(payload);
                } else if ("MARKET_STATISTICS".equals(messageType)) {
                    processMarketStatistics(payload);
                }

            } catch (Exception e) {
                log.error("处理tick消息失败", e);
            }
        };
    }

    /**
     * 处理成交执行消息
     */
    private void processTradeExecution(Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        BigDecimal price = new BigDecimal(payload.get("price").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Long timestamp = (Long) payload.get("timestamp");
        Integer isBuyerMaker = (Integer) payload.get("isBuyerMaker");
        String tradeId = (String) payload.get("tradeId");

        // 更新行情数据
        marketTickerService.updateTicker(symbol, price, price, price, amount, price.multiply(amount), 1);

        // 添加成交记录
        marketTradeService.addTrade(tradeId, symbol, price, amount, timestamp, isBuyerMaker);

        // 更新K线数据
        marketKlineService.updateKline(symbol, "1m", timestamp, price, amount);

        log.info("处理成交执行: {} {} {} {}", symbol, price, amount, isBuyerMaker);
    }

    /**
     * 处理订单更新消息
     */
    private void processOrderUpdate(Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        BigDecimal price = new BigDecimal(payload.get("price").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String orderType = (String) payload.get("orderType");
        String direction = (String) payload.get("direction");

        // 根据订单更新深度数据
        // 这里需要实现深度数据的更新逻辑
        updateMarketDepth(symbol, orderType, direction, price, amount);

        log.debug("处理订单更新: {} {} {} {}", symbol, orderType, direction, price);
    }

    /**
     * 处理交易对更新消息
     */
    private void processPairUpdate(Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        Integer status = (Integer) payload.get("status");

        // 更新本地交易对状态
        // 这里可以实现交易对状态的同步

        log.info("处理交易对更新: {} {}", symbol, status);
    }

    /**
     * 处理行情更新消息
     */
    private void processTickerUpdate(Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        BigDecimal lastPrice = new BigDecimal(payload.get("lastPrice").toString());
        BigDecimal highPrice = new BigDecimal(payload.get("highPrice").toString());
        BigDecimal lowPrice = new BigDecimal(payload.get("lowPrice").toString());
        BigDecimal volume = new BigDecimal(payload.get("volume").toString());
        BigDecimal quoteVolume = new BigDecimal(payload.get("quoteVolume").toString());
        Integer count = (Integer) payload.get("count");

        marketTickerService.updateTicker(symbol, lastPrice, highPrice, lowPrice, volume, quoteVolume, count);
        log.debug("处理行情更新: {}", symbol);
    }

    /**
     * 处理成交更新消息
     */
    private void processTradeUpdate(Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        BigDecimal price = new BigDecimal(payload.get("price").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Long timestamp = (Long) payload.get("timestamp");
        Integer isBuyerMaker = (Integer) payload.get("isBuyerMaker");
        String tradeId = (String) payload.get("tradeId");

        marketTradeService.addTrade(tradeId, symbol, price, amount, timestamp, isBuyerMaker);
        log.debug("处理成交更新: {} {}", symbol, tradeId);
    }

    /**
     * 处理深度更新消息
     */
    private void processDepthUpdate(Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        List<BigDecimal[]> bids = (List<BigDecimal[]>) payload.get("bids");
        List<BigDecimal[]> asks = (List<BigDecimal[]>) payload.get("asks");
        Long timestamp = (Long) payload.get("timestamp");

        marketDepthService.updateMarketDepth(symbol, bids, asks, timestamp);
        log.debug("处理深度更新: {}", symbol);
    }

    /**
     * 处理K线更新消息
     */
    private void processKlineUpdate(Map<String, Object> payload) {
        String symbol = (String) payload.get("symbol");
        String interval = (String) payload.get("interval");
        Long openTime = (Long) payload.get("openTime");
        BigDecimal openPrice = new BigDecimal(payload.get("openPrice").toString());
        BigDecimal highPrice = new BigDecimal(payload.get("highPrice").toString());
        BigDecimal lowPrice = new BigDecimal(payload.get("lowPrice").toString());
        BigDecimal closePrice = new BigDecimal(payload.get("closePrice").toString());
        BigDecimal volume = new BigDecimal(payload.get("volume").toString());
        Integer tradesCount = (Integer) payload.get("tradesCount");

        marketKlineService.updateKline(symbol, interval, openTime, closePrice, volume);
        log.debug("处理K线更新: {} {}", symbol, interval);
    }

    /**
     * 处理市场统计消息
     */
    private void processMarketStatistics(Map<String, Object> payload) {
        // 处理市场统计数据
        log.debug("处理市场统计消息");
    }

    /**
     * 处理系统命令消息
     */
    private void processSystemCommand(Map<String, Object> payload) {
        String command = (String) payload.get("command");

        switch (command) {
            case "RESET_DAILY":
                marketTickerService.resetDailyTickers();
                log.info("执行重置日行情数据命令");
                break;
            case "CLEAN_CACHE":
                marketDepthService.clearDepthCache();
                log.info("执行清除深度缓存命令");
                break;
            case "SYNC_PAIRS":
                // 同步交易对数据
                log.info("执行同步交易对数据命令");
                break;
        }
    }

    /**
     * 更新市场深度数据
     */
    private void updateMarketDepth(String symbol, String orderType, String direction, BigDecimal price, BigDecimal amount) {
        try {
            // 这里应该实现深度数据的更新逻辑
            // 由于深度数据比较复杂，这里只是简单示例
            // 实际项目中需要实现完整的深度数据管理

            // 获取当前深度数据
            // List<BigDecimal[]> currentBids = getCurrentBids(symbol);
            // List<BigDecimal[]> currentAsks = getCurrentAsks(symbol);

            // 根据订单类型和方向更新深度数据
            // ...

            // marketDepthService.updateMarketDepth(symbol, updatedBids, updatedAsks, System.currentTimeMillis());

        } catch (Exception e) {
            log.error("更新深度数据失败: {}", symbol, e);
        }
    }
}