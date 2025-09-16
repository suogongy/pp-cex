package com.cex.trade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cex.trade.entity.TradePair;
import com.cex.trade.mapper.TradePairMapper;
import com.cex.trade.service.TradePairService;
import com.cex.trade.dto.TradePairVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TradePairServiceImpl extends ServiceImpl<TradePairMapper, TradePair> implements TradePairService {

    @Override
    public IPage<TradePairVO> getTradePairPage(Page<TradePair> page, String symbol, Integer status) {
        return baseMapper.selectTradePairVOPage(page, symbol, status);
    }

    @Override
    public List<TradePairVO> getActiveTradePairs() {
        return baseMapper.selectActiveTradePairs();
    }

    @Override
    public TradePairVO getTradePairBySymbol(String symbol) {
        return baseMapper.selectTradePairVOBySymbol(symbol);
    }

    @Override
    public TradePairVO getTradePairById(Long id) {
        TradePair tradePair = getById(id);
        if (tradePair == null) {
            return null;
        }
        TradePairVO vo = new TradePairVO();
        BeanUtils.copyProperties(tradePair, vo);
        return vo;
    }

    @Override
    public boolean addTradePair(TradePair tradePair) {
        validateTradePair(tradePair);
        return save(tradePair);
    }

    @Override
    public boolean updateTradePair(TradePair tradePair) {
        validateTradePair(tradePair);
        return updateById(tradePair);
    }

    @Override
    public boolean deleteTradePair(Long id) {
        return removeById(id);
    }

    private void validateTradePair(TradePair tradePair) {
        if (!StringUtils.hasText(tradePair.getSymbol())) {
            throw new IllegalArgumentException("交易对符号不能为空");
        }
        if (!StringUtils.hasText(tradePair.getBaseCoin())) {
            throw new IllegalArgumentException("基础币种不能为空");
        }
        if (!StringUtils.hasText(tradePair.getQuoteCoin())) {
            throw new IllegalArgumentException("计价币种不能为空");
        }
        if (tradePair.getFeeRate() == null || tradePair.getFeeRate().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("手续费率不能为负数");
        }
        if (tradePair.getMinAmount() == null || tradePair.getMinAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("最小数量必须大于0");
        }
        if (tradePair.getMaxAmount() == null || tradePair.getMaxAmount().compareTo(tradePair.getMinAmount()) < 0) {
            throw new IllegalArgumentException("最大数量不能小于最小数量");
        }
    }
}