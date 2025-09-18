package com.ppcex.market.service;

import com.ppcex.market.entity.MarketTicker;
import com.ppcex.market.dto.MarketTickerVO;

import java.math.BigDecimal;
import java.util.List;

public interface MarketTickerService {

    MarketTickerVO getTickerBySymbol(String symbol);

    List<MarketTickerVO> getAllTickers();

    void updateTicker(String symbol, BigDecimal lastPrice, BigDecimal highPrice,
                     BigDecimal lowPrice, BigDecimal volume, BigDecimal quoteVolume, Integer count);

    void batchUpdateTickers(List<MarketTicker> tickers);

    void initializeDailyTicker(String symbol, BigDecimal openPrice);

    void resetDailyTickers();

    List<MarketTickerVO> getTopGainers(Integer limit);

    List<MarketTickerVO> getTopLosers(Integer limit);

    List<MarketTickerVO> getTopVolume(Integer limit);
}