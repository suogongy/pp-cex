package com.ppcex.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.market.entity.MarketKline;
import com.ppcex.market.mapper.MarketKlineMapper;
import com.ppcex.market.service.MarketKlineService;
import com.ppcex.market.dto.KlineDataVO;
import com.ppcex.market.enums.KlineIntervalEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketKlineServiceImpl extends ServiceImpl<MarketKlineMapper, MarketKline> implements MarketKlineService {

    @Autowired
    private MarketKlineMapper marketKlineMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String KLINE_CACHE_PREFIX = "kline:";
    private static final int KLINE_CACHE_LIMIT = 1000;

    @Override
    @Cacheable(value = "klineData", key = "#symbol + ':' + #interval + ':' + #startTime + ':' + #endTime + ':' + #limit")
    public List<MarketKline> getKlineData(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        String cacheKey = KLINE_CACHE_PREFIX + symbol + ":" + interval;

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<MarketKline> cachedKlines = (List<MarketKline>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedKlines != null) {
            return filterKlinesByTime(cachedKlines, startTime, endTime, limit);
        }

        // 从数据库获取
        List<MarketKline> klines = marketKlineMapper.getKlineData(symbol, interval, startTime, endTime, limit);

        // 缓存结果
        if (klines != null && !klines.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, klines, 5, TimeUnit.MINUTES);
        }

        return klines;
    }

    @Override
    @Cacheable(value = "latestKline", key = "#symbol + ':' + #interval")
    public MarketKline getLatestKline(String symbol, String interval) {
        return marketKlineMapper.getLatestKline(symbol, interval);
    }

    @Override
    @CacheEvict(value = {"klineData", "latestKline"}, key = "#symbol + ':' + #interval")
    @Transactional
    public void updateKline(String symbol, String interval, Long timestamp, BigDecimal price, BigDecimal amount) {
        KlineIntervalEnum intervalEnum = KlineIntervalEnum.getByCode(interval);
        if (intervalEnum == null) {
            log.warn("不支持的K线间隔: {}", interval);
            return;
        }

        Long klineOpenTime = calculateKlineOpenTime(timestamp, intervalEnum.getSeconds());
        MarketKline existingKline = marketKlineMapper.getLatestKline(symbol, interval);

        if (existingKline != null && existingKline.getOpenTime().equals(klineOpenTime)) {
            // 更新现有K线
            existingKline.setHighPrice(existingKline.getHighPrice().max(price));
            existingKline.setLowPrice(existingKline.getLowPrice().min(price));
            existingKline.setClosePrice(price);
            existingKline.setVolume(existingKline.getVolume().add(amount));
            existingKline.setQuoteVolume(existingKline.getQuoteVolume().add(price.multiply(amount)));
            existingKline.setTradesCount(existingKline.getTradesCount() + 1);

            marketKlineMapper.updateKline(existingKline);
        } else {
            // 创建新K线
            MarketKline newKline = new MarketKline();
            newKline.setSymbol(symbol);
            newKline.setInterval(interval);
            newKline.setOpenTime(klineOpenTime);
            newKline.setCloseTime(klineOpenTime + intervalEnum.getSeconds() * 1000L);
            newKline.setOpenPrice(price);
            newKline.setHighPrice(price);
            newKline.setLowPrice(price);
            newKline.setClosePrice(price);
            newKline.setVolume(amount);
            newKline.setQuoteVolume(price.multiply(amount));
            newKline.setTradesCount(1);

            marketKlineMapper.batchInsertKlines(List.of(newKline));
        }

        // 清除缓存
        String cacheKey = KLINE_CACHE_PREFIX + symbol + ":" + interval;
        redisTemplate.delete(cacheKey);
    }

    @Override
    @CacheEvict(value = {"klineData", "latestKline"}, allEntries = true)
    @Transactional
    public void batchUpdateKlines(List<MarketKline> klines) {
        marketKlineMapper.batchInsertKlines(klines);

        // 清除所有相关缓存
        for (MarketKline kline : klines) {
            String cacheKey = KLINE_CACHE_PREFIX + kline.getSymbol() + ":" + kline.getInterval();
            redisTemplate.delete(cacheKey);
        }
    }

    @Override
    @CacheEvict(value = {"klineData", "latestKline"}, allEntries = true)
    @Transactional
    public void generateKlineData(String symbol, String interval, Long startTime, Long endTime) {
        KlineIntervalEnum intervalEnum = KlineIntervalEnum.getByCode(interval);
        if (intervalEnum == null) {
            log.warn("不支持的K线间隔: {}", interval);
            return;
        }

        List<MarketKline> klines = new ArrayList<>();
        Long currentTime = startTime;

        while (currentTime < endTime) {
            Long openTime = calculateKlineOpenTime(currentTime, intervalEnum.getSeconds());
            Long closeTime = openTime + intervalEnum.getSeconds() * 1000L;

            MarketKline kline = new MarketKline();
            kline.setSymbol(symbol);
            kline.setInterval(interval);
            kline.setOpenTime(openTime);
            kline.setCloseTime(closeTime);
            kline.setOpenPrice(BigDecimal.ZERO);
            kline.setHighPrice(BigDecimal.ZERO);
            kline.setLowPrice(BigDecimal.ZERO);
            kline.setClosePrice(BigDecimal.ZERO);
            kline.setVolume(BigDecimal.ZERO);
            kline.setQuoteVolume(BigDecimal.ZERO);
            kline.setTradesCount(0);

            klines.add(kline);
            currentTime = closeTime;
        }

        marketKlineMapper.batchInsertKlines(klines);
    }

    @Override
    @CacheEvict(value = {"klineData", "latestKline"}, allEntries = true)
    @Transactional
    public void cleanOldKlines() {
        // 保留最近30天的K线数据
        Long cutoffTime = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;

        for (KlineIntervalEnum interval : KlineIntervalEnum.values()) {
            marketKlineMapper.deleteOldKlines(null, interval.getCode(), cutoffTime);
        }
    }

    @Override
    @Cacheable(value = "klineHistory", key = "#symbol + ':' + #interval + ':' + #limit")
    public List<MarketKline> getKlineHistory(String symbol, String interval, Integer limit) {
        return marketKlineMapper.getKlineData(symbol, interval, null, null, limit);
    }

    private Long calculateKlineOpenTime(Long timestamp, int intervalSeconds) {
        return (timestamp / (intervalSeconds * 1000L)) * (intervalSeconds * 1000L);
    }

    private List<MarketKline> filterKlinesByTime(List<MarketKline> klines, Long startTime, Long endTime, Integer limit) {
        return klines.stream()
                .filter(kline -> (startTime == null || kline.getOpenTime() >= startTime) &&
                               (endTime == null || kline.getOpenTime() <= endTime))
                .limit(limit != null ? limit : Long.MAX_VALUE)
                .collect(Collectors.toList());
    }
}