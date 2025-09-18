package com.ppcex.market.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppcex.market.entity.MarketPair;
import com.ppcex.market.dto.MarketPairVO;

import java.util.List;

public interface MarketPairService {

    IPage<MarketPairVO> getMarketPairPage(Page<MarketPair> page, String symbol, Integer status);

    List<MarketPairVO> getActiveMarketPairs();

    MarketPairVO getMarketPairBySymbol(String symbol);

    MarketPairVO getMarketPairById(Long id);

    boolean addMarketPair(MarketPair marketPair);

    boolean updateMarketPair(MarketPair marketPair);

    boolean deleteMarketPair(Long id);

    boolean syncFromTradeService();

    List<MarketPairVO> getAllMarketPairs();
}