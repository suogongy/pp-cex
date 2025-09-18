package com.ppcex.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.market.entity.MarketTicker;
import com.ppcex.market.mapper.MarketTickerMapper;
import com.ppcex.market.service.MarketTickerService;
import com.ppcex.market.dto.MarketTickerVO;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketTickerServiceImpl extends ServiceImpl<MarketTickerMapper, MarketTicker> implements MarketTickerService {

    @Autowired
    private MarketTickerMapper marketTickerMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TICKER_CACHE_PREFIX = "ticker:";
    private static final String TICKER_LIST_CACHE = "ticker:list";

    @Override
    @Cacheable(value = "tickerBySymbol", key = "#symbol", unless = "#result == null")
    public MarketTickerVO getTickerBySymbol(String symbol) {
        MarketTickerVO tickerVO = marketTickerMapper.getTickerBySymbol(symbol);
        if (tickerVO != null) {
            enrichTickerVO(tickerVO);
        }
        return tickerVO;
    }

    @Override
    @Cacheable(value = "allTickers", unless = "#result == null || #result.isEmpty()")
    public List<MarketTickerVO> getAllTickers() {
        List<MarketTickerVO> tickers = marketTickerMapper.getAllTickers();
        return tickers.stream()
                .peek(this::enrichTickerVO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"tickerBySymbol", "allTickers"}, key = "#symbol")
    @Transactional
    public void updateTicker(String symbol, BigDecimal lastPrice, BigDecimal highPrice,
                           BigDecimal lowPrice, BigDecimal volume, BigDecimal quoteVolume, Integer count) {
        marketTickerMapper.updateTickerBySymbol(symbol, lastPrice, highPrice, lowPrice, volume, quoteVolume, count);

        // 异步更新缓存
        MarketTickerVO tickerVO = getTickerBySymbol(symbol);
        if (tickerVO != null) {
            redisTemplate.opsForValue().set(TICKER_CACHE_PREFIX + symbol, tickerVO, 1, TimeUnit.MINUTES);
        }
        redisTemplate.delete(TICKER_LIST_CACHE);
    }

    @Override
    @CacheEvict(value = {"tickerBySymbol", "allTickers"}, allEntries = true)
    @Transactional
    public void batchUpdateTickers(List<MarketTicker> tickers) {
        marketTickerMapper.batchUpdateTickers(tickers);

        // 清除所有相关缓存
        for (MarketTicker ticker : tickers) {
            redisTemplate.delete(TICKER_CACHE_PREFIX + ticker.getSymbol());
        }
        redisTemplate.delete(TICKER_LIST_CACHE);
    }

    @Override
    @CacheEvict(value = {"tickerBySymbol", "allTickers"}, key = "#symbol")
    @Transactional
    public void initializeDailyTicker(String symbol, BigDecimal openPrice) {
        MarketTicker ticker = new MarketTicker();
        ticker.setSymbol(symbol);
        ticker.setLastPrice(openPrice);
        ticker.setOpenPrice(openPrice);
        ticker.setHighPrice(openPrice);
        ticker.setLowPrice(openPrice);
        ticker.setVolume(BigDecimal.ZERO);
        ticker.setQuoteVolume(BigDecimal.ZERO);
        ticker.setPriceChange(BigDecimal.ZERO);
        ticker.setPriceChangePercent(BigDecimal.ZERO);
        ticker.setCount(0);
        ticker.setLastUpdateTime(LocalDateTime.now());

        saveOrUpdate(ticker, new QueryWrapper<MarketTicker>().eq("symbol", symbol));
    }

    @Override
    @CacheEvict(value = {"tickerBySymbol", "allTickers"}, allEntries = true)
    @Transactional
    public void resetDailyTickers() {
        List<MarketTicker> tickers = list();
        for (MarketTicker ticker : tickers) {
            ticker.setOpenPrice(ticker.getLastPrice());
            ticker.setHighPrice(ticker.getLastPrice());
            ticker.setLowPrice(ticker.getLastPrice());
            ticker.setVolume(BigDecimal.ZERO);
            ticker.setQuoteVolume(BigDecimal.ZERO);
            ticker.setPriceChange(BigDecimal.ZERO);
            ticker.setPriceChangePercent(BigDecimal.ZERO);
            ticker.setCount(0);
            ticker.setLastUpdateTime(LocalDateTime.now());
        }
        updateBatchById(tickers);

        // 清除所有缓存
        redisTemplate.delete(TICKER_LIST_CACHE);
    }

    @Override
    @Cacheable(value = "topGainers", unless = "#result == null || #result.isEmpty()")
    public List<MarketTickerVO> getTopGainers(Integer limit) {
        List<MarketTickerVO> allTickers = getAllTickers();
        return allTickers.stream()
                .filter(ticker -> ticker.getPriceChangePercent() != null && ticker.getPriceChangePercent().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(MarketTickerVO::getPriceChangePercent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "topLosers", unless = "#result == null || #result.isEmpty()")
    public List<MarketTickerVO> getTopLosers(Integer limit) {
        List<MarketTickerVO> allTickers = getAllTickers();
        return allTickers.stream()
                .filter(ticker -> ticker.getPriceChangePercent() != null && ticker.getPriceChangePercent().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(MarketTickerVO::getPriceChangePercent))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "topVolume", unless = "#result == null || #result.isEmpty()")
    public List<MarketTickerVO> getTopVolume(Integer limit) {
        List<MarketTickerVO> allTickers = getAllTickers();
        return allTickers.stream()
                .filter(ticker -> ticker.getQuoteVolume() != null && ticker.getQuoteVolume().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(MarketTickerVO::getQuoteVolume).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void enrichTickerVO(MarketTickerVO tickerVO) {
        if (tickerVO == null) return;

        // 设置价格变化类型
        if (tickerVO.getPriceChange() != null) {
            if (tickerVO.getPriceChange().compareTo(BigDecimal.ZERO) > 0) {
                tickerVO.setPriceChangeType("UP");
            } else if (tickerVO.getPriceChange().compareTo(BigDecimal.ZERO) < 0) {
                tickerVO.setPriceChangeType("DOWN");
            } else {
                tickerVO.setPriceChangeType("EQUAL");
            }
        }

        // 设置价格变化百分比文本
        if (tickerVO.getPriceChangePercent() != null) {
            String sign = tickerVO.getPriceChangePercent().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            tickerVO.setPriceChangePercentText(sign + tickerVO.getPriceChangePercent().setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
        }
    }
}