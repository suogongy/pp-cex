package com.ppcex.market.service;

import com.ppcex.market.entity.MarketKline;
import com.ppcex.market.dto.KlineDataVO;

import java.math.BigDecimal;
import java.util.List;

public interface MarketKlineService {

    List<MarketKline> getKlineData(String symbol, String interval, Long startTime, Long endTime, Integer limit);

    MarketKline getLatestKline(String symbol, String interval);

    void updateKline(String symbol, String interval, Long timestamp, BigDecimal price, BigDecimal amount);

    void batchUpdateKlines(List<MarketKline> klines);

    void generateKlineData(String symbol, String interval, Long startTime, Long endTime);

    void cleanOldKlines();

    List<MarketKline> getKlineHistory(String symbol, String interval, Integer limit);
}