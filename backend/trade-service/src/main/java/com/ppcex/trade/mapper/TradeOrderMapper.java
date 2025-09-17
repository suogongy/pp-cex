package com.cex.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.trade.entity.TradeOrder;
import com.cex.trade.dto.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    IPage<OrderVO> selectOrderVOPage(Page<TradeOrder> page, @Param("userId") Long userId, @Param("symbol") String symbol,
                                     @Param("status") Integer status, @Param("orderType") Integer orderType);

    OrderVO selectOrderVOByOrderNo(@Param("orderNo") String orderNo);

    List<OrderVO> selectActiveOrdersBySymbol(@Param("symbol") String symbol);

    List<OrderVO> selectUserActiveOrders(@Param("userId") Long userId);
}