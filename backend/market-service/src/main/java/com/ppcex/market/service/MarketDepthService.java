package com.ppcex.market.service;

import com.ppcex.market.dto.MarketDepthVO;

import java.math.BigDecimal;
import java.util.List;

public interface MarketDepthService {

    MarketDepthVO getMarketDepth(String symbol, Integer limit);

    void updateMarketDepth(String symbol, List<BigDecimal[]> bids, List<BigDecimal[]> asks, Long timestamp);

    void saveMarketDepthSnapshot(String symbol, List<BigDecimal[]> bids, List<BigDecimal[]> asks, Long timestamp);

    List<MarketDepthVO> getDepthHistory(String symbol, Integer limit);

    void clearDepthCache();
}