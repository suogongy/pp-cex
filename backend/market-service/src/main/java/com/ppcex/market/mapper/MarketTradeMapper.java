package com.ppcex.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.market.entity.MarketTrade;
import com.ppcex.market.dto.MarketTradeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketTradeMapper extends BaseMapper<MarketTrade> {

    List<MarketTradeVO> getRecentTrades(@Param("symbol") String symbol, @Param("limit") Integer limit);

    int batchInsertTrades(@Param("trades") List<MarketTrade> trades);

    int deleteOldTrades(@Param("symbol") String symbol, @Param("beforeTime") Long beforeTime);
}