package com.ppcex.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ppcex.trade.entity.TradeDetail;
import com.ppcex.trade.dto.TradeDetailVO;

import java.util.List;

public interface TradeDetailService extends IService<TradeDetail> {

    IPage<TradeDetailVO> getTradeDetailPage(Page<TradeDetail> page, Long userId, String symbol, String startTime, String endTime);

    List<TradeDetailVO> getRecentTradesBySymbol(String symbol, Integer limit);

    List<TradeDetailVO> getUserTrades(Long userId, String symbol);

    boolean createTradeDetail(TradeDetail tradeDetail);

    String generateTradeNo();
}