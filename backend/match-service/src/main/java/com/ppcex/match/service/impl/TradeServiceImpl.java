package com.ppcex.match.service.impl;

import com.ppcex.match.entity.MatchOrder;
import com.ppcex.match.entity.TradeRecord;
import com.ppcex.match.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TRADE_RECORD_PREFIX = "match:trade:record:";
    private static final String TRADE_HISTORY_PREFIX = "match:trade:history:";

    @Override
    public void processTrade(TradeRecord tradeRecord, MatchOrder buyOrder, MatchOrder sellOrder) {
        try {
            saveTradeRecord(tradeRecord);
            updateOrderStatus(buyOrder);
            updateOrderStatus(sellOrder);
            notifyTradeUpdate(tradeRecord);
            broadcastTrade(tradeRecord);
        } catch (Exception e) {
            log.error("处理交易记录失败: {}", tradeRecord.getTradeNo(), e);
            throw new RuntimeException("处理交易记录失败", e);
        }
    }

    @Override
    public void saveTradeRecord(TradeRecord tradeRecord) {
        try {
            String recordKey = TRADE_RECORD_PREFIX + tradeRecord.getTradeNo();
            String historyKey = TRADE_HISTORY_PREFIX + tradeRecord.getSymbol();

            redisTemplate.opsForValue().set(recordKey, tradeRecord, 24, TimeUnit.HOURS);
            redisTemplate.opsForList().rightPush(historyKey, tradeRecord);
            redisTemplate.expire(historyKey, 1, TimeUnit.DAYS);

            log.info("交易记录已保存: {}", tradeRecord.getTradeNo());
        } catch (Exception e) {
            log.error("保存交易记录失败: {}", tradeRecord.getTradeNo(), e);
        }
    }

    @Override
    public void updateOrderStatus(MatchOrder order) {
        try {
            String orderKey = "match:order:" + order.getOrderNo();
            redisTemplate.opsForValue().set(orderKey, order, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("更新订单状态失败: {}", order.getOrderNo(), e);
        }
    }

    @Override
    public void notifyTradeUpdate(TradeRecord tradeRecord) {
        try {
            Map<String, Object> tradeUpdate = new HashMap<>();
            tradeUpdate.put("tradeNo", tradeRecord.getTradeNo());
            tradeUpdate.put("symbol", tradeRecord.getSymbol());
            tradeUpdate.put("price", tradeRecord.getPrice());
            tradeUpdate.put("amount", tradeRecord.getAmount());
            tradeUpdate.put("makerOrderId", tradeRecord.getMakerOrderId());
            tradeUpdate.put("takerOrderId", tradeRecord.getTakerOrderId());
            tradeUpdate.put("makerUserId", tradeRecord.getMakerUserId());
            tradeUpdate.put("takerUserId", tradeRecord.getTakerUserId());
            tradeUpdate.put("timestamp", System.currentTimeMillis());

            redisTemplate.convertAndSend("trade:update:" + tradeRecord.getSymbol(), tradeUpdate);
        } catch (Exception e) {
            log.error("通知交易更新失败: {}", tradeRecord.getTradeNo(), e);
        }
    }

    private void broadcastTrade(TradeRecord tradeRecord) {
        try {
            Map<String, Object> tradeBroadcast = new HashMap<>();
            tradeBroadcast.put("type", "trade");
            tradeBroadcast.put("data", tradeRecord);
            tradeBroadcast.put("timestamp", System.currentTimeMillis());

            redisTemplate.convertAndSend("market:broadcast", tradeBroadcast);
        } catch (Exception e) {
            log.error("广播交易失败: {}", tradeRecord.getTradeNo(), e);
        }
    }
}