package com.cex.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.trade.entity.TradePair;
import com.cex.trade.dto.TradePairVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradePairMapper extends BaseMapper<TradePair> {

    IPage<TradePairVO> selectTradePairVOPage(Page<TradePair> page, @Param("symbol") String symbol, @Param("status") Integer status);

    List<TradePairVO> selectActiveTradePairs();

    TradePairVO selectTradePairVOBySymbol(@Param("symbol") String symbol);
}