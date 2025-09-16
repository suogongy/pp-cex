package com.ppcex.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.finance.entity.FinancialFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FinancialFlowRepository extends BaseMapper<FinancialFlow> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM financial_flow WHERE user_id = #{userId} AND coin_id = #{coinId} AND status = 1")
    BigDecimal getTotalAmountByUserAndCoin(@Param("userId") Long userId, @Param("coinId") String coinId);

    @Select("SELECT COALESCE(SUM(fee), 0) FROM financial_flow WHERE business_type = 5 AND status = 1 AND create_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal getTotalFeeByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM financial_flow WHERE business_type = #{businessType} AND status = 1 AND create_time BETWEEN #{startTime} AND #{endTime}")
    BigDecimal getTotalAmountByBusinessTypeAndTimeRange(@Param("businessType") Integer businessType, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM financial_flow WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<FinancialFlow> getUserRecentFlows(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT * FROM financial_flow WHERE ref_order_no = #{orderNo}")
    FinancialFlow getFlowByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM financial_flow WHERE user_id = #{userId} AND create_time >= #{since}")
    Long getUserFlowCountSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}