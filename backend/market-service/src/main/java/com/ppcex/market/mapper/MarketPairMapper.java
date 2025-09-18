package com.ppcex.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppcex.market.entity.MarketPair;
import com.ppcex.market.dto.MarketPairVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketPairMapper extends BaseMapper<MarketPair> {

    IPage<MarketPairVO> getMarketPairPage(Page<MarketPair> page, @Param("symbol") String symbol, @Param("status") Integer status);

    List<MarketPairVO> getActiveMarketPairs();

    MarketPairVO getMarketPairBySymbol(@Param("symbol") String symbol);

    MarketPairVO getMarketPairById(@Param("id") Long id);
}