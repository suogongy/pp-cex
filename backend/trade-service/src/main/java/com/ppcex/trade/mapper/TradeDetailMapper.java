package com.ppcex.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppcex.trade.entity.TradeDetail;
import com.ppcex.trade.dto.TradeDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradeDetailMapper extends BaseMapper<TradeDetail> {

    IPage<TradeDetailVO> selectTradeDetailVOPage(Page<TradeDetail> page, @Param("userId") Long userId,
                                                  @Param("symbol") String symbol, @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    List<TradeDetailVO> selectRecentTradesBySymbol(@Param("symbol") String symbol, @Param("limit") Integer limit);

    List<TradeDetailVO> selectUserTrades(@Param("userId") Long userId, @Param("symbol") String symbol);
}