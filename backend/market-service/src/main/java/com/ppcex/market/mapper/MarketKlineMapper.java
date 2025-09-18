package com.ppcex.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.market.entity.MarketKline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketKlineMapper extends BaseMapper<MarketKline> {

    List<MarketKline> getKlineData(@Param("symbol") String symbol,
                                 @Param("interval") String interval,
                                 @Param("startTime") Long startTime,
                                 @Param("endTime") Long endTime,
                                 @Param("limit") Integer limit);

    MarketKline getLatestKline(@Param("symbol") String symbol, @Param("interval") String interval);

    int batchInsertKlines(@Param("klines") List<MarketKline> klines);

    int updateKline(@Param("kline") MarketKline kline);

    int deleteOldKlines(@Param("symbol") String symbol, @Param("interval") String interval, @Param("beforeTime") Long beforeTime);
}