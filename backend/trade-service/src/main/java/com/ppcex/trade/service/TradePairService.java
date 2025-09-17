package com.cex.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cex.trade.entity.TradePair;
import com.cex.trade.dto.TradePairVO;

import java.util.List;

public interface TradePairService extends IService<TradePair> {

    IPage<TradePairVO> getTradePairPage(Page<TradePair> page, String symbol, Integer status);

    List<TradePairVO> getActiveTradePairs();

    TradePairVO getTradePairBySymbol(String symbol);

    TradePairVO getTradePairById(Long id);

    boolean addTradePair(TradePair tradePair);

    boolean updateTradePair(TradePair tradePair);

    boolean deleteTradePair(Long id);
}