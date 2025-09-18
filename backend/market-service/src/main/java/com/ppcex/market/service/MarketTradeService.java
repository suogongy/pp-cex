package com.ppcex.market.service;

import com.ppcex.market.entity.MarketTrade;
import com.ppcex.market.dto.MarketTradeVO;

import java.math.BigDecimal;
import java.util.List;

public interface MarketTradeService {

    List<MarketTradeVO> getRecentTrades(String symbol, Integer limit);

    void addTrade(String tradeId, String symbol, BigDecimal price, BigDecimal amount, Long timestamp, Integer isBuyerMaker);

    void batchAddTrades(List<MarketTrade> trades);

    void cleanOldTrades();

    List<MarketTradeVO> getTradeHistory(String symbol, Long startTime, Long endTime, Integer limit);
}