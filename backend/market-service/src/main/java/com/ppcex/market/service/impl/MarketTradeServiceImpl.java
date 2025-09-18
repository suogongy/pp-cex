package com.ppcex.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.market.entity.MarketTrade;
import com.ppcex.market.mapper.MarketTradeMapper;
import com.ppcex.market.service.MarketTradeService;
import com.ppcex.market.dto.MarketTradeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketTradeServiceImpl extends ServiceImpl<MarketTradeMapper, MarketTrade> implements MarketTradeService {

    @Autowired
    private MarketTradeMapper marketTradeMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TRADE_CACHE_PREFIX = "trade:";
    private static final String TRADE_LIST_CACHE_PREFIX = "trade:list:";
    private static final int TRADE_CACHE_LIMIT = 1000;

    @Override
    @Cacheable(value = "recentTrades", key = "#symbol + ':' + #limit")
    public List<MarketTradeVO> getRecentTrades(String symbol, Integer limit) {
        String cacheKey = TRADE_LIST_CACHE_PREFIX + symbol;

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<MarketTradeVO> cachedTrades = (List<MarketTradeVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedTrades != null) {
            return cachedTrades.stream().limit(limit).collect(Collectors.toList());
        }

        // 从数据库获取
        List<MarketTradeVO> trades = marketTradeMapper.getRecentTrades(symbol, limit);

        // 缓存结果
        if (trades != null && !trades.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, trades, 2, TimeUnit.MINUTES);
        }

        return trades;
    }

    @Override
    @CacheEvict(value = "recentTrades", key = "#symbol")
    @Transactional
    public void addTrade(String tradeId, String symbol, BigDecimal price, BigDecimal amount, Long timestamp, Integer isBuyerMaker) {
        MarketTrade trade = new MarketTrade();
        trade.setTradeId(tradeId != null ? tradeId : UUID.randomUUID().toString().replace("-", ""));
        trade.setSymbol(symbol);
        trade.setPrice(price);
        trade.setAmount(amount);
        trade.setQuoteVolume(price.multiply(amount));
        trade.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());
        trade.setIsBuyerMaker(isBuyerMaker);

        save(trade);

        // 更新缓存
        String cacheKey = TRADE_LIST_CACHE_PREFIX + symbol;
        redisTemplate.delete(cacheKey);

        log.info("添加成交记录: {} {} {} {}", symbol, price, amount, isBuyerMaker == 1 ? "SELL" : "BUY");
    }

    @Override
    @CacheEvict(value = "recentTrades", allEntries = true)
    @Transactional
    public void batchAddTrades(List<MarketTrade> trades) {
        marketTradeMapper.batchInsertTrades(trades);

        // 清除所有相关缓存
        for (MarketTrade trade : trades) {
            String cacheKey = TRADE_LIST_CACHE_PREFIX + trade.getSymbol();
            redisTemplate.delete(cacheKey);
        }
    }

    @Override
    @CacheEvict(value = "recentTrades", allEntries = true)
    @Transactional
    public void cleanOldTrades() {
        // 保留最近24小时的成交记录
        Long cutoffTime = System.currentTimeMillis() - 24L * 60 * 60 * 1000;

        // 获取所有活跃的交易对
        List<String> symbols = list().stream()
                .map(MarketTrade::getSymbol)
                .distinct()
                .collect(Collectors.toList());

        for (String symbol : symbols) {
            marketTradeMapper.deleteOldTrades(symbol, cutoffTime);
        }

        // 清除所有缓存
        for (String symbol : symbols) {
            String cacheKey = TRADE_LIST_CACHE_PREFIX + symbol;
            redisTemplate.delete(cacheKey);
        }
    }

    @Override
    @Cacheable(value = "tradeHistory", key = "#symbol + ':' + #startTime + ':' + #endTime + ':' + #limit")
    public List<MarketTradeVO> getTradeHistory(String symbol, Long startTime, Long endTime, Integer limit) {
        // 从数据库查询历史成交记录
        return marketTradeMapper.getRecentTrades(symbol, limit);
    }

    private MarketTradeVO convertToVO(MarketTrade trade) {
        MarketTradeVO vo = new MarketTradeVO();
        BeanUtils.copyProperties(trade, vo);

        // 设置交易类型
        if (trade.getIsBuyerMaker() != null) {
            vo.setIsBuyerMaker(trade.getIsBuyerMaker() == 1);
            vo.setTradeType(trade.getIsBuyerMaker() == 1 ? "SELL" : "BUY");
        }

        return vo;
    }
}