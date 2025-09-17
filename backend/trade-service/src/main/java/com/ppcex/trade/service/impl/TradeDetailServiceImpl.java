package com.ppcex.trade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.trade.entity.TradeDetail;
import com.ppcex.trade.mapper.TradeDetailMapper;
import com.ppcex.trade.service.TradeDetailService;
import com.ppcex.trade.dto.TradeDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeDetailServiceImpl extends ServiceImpl<TradeDetailMapper, TradeDetail> implements TradeDetailService {

    @Override
    public IPage<TradeDetailVO> getTradeDetailPage(Page<TradeDetail> page, Long userId, String symbol, String startTime, String endTime) {
        return baseMapper.selectTradeDetailVOPage(page, userId, symbol, startTime, endTime);
    }

    @Override
    public List<TradeDetailVO> getRecentTradesBySymbol(String symbol, Integer limit) {
        return baseMapper.selectRecentTradesBySymbol(symbol, limit != null ? limit : 50);
    }

    @Override
    public List<TradeDetailVO> getUserTrades(Long userId, String symbol) {
        return baseMapper.selectUserTrades(userId, symbol);
    }

    @Override
    public boolean createTradeDetail(TradeDetail tradeDetail) {
        if (tradeDetail.getTradeNo() == null) {
            tradeDetail.setTradeNo(generateTradeNo());
        }
        if (tradeDetail.getCreateTime() == null) {
            tradeDetail.setCreateTime(LocalDateTime.now());
        }
        return save(tradeDetail);
    }

    @Override
    public String generateTradeNo() {
        return "TRD" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
}