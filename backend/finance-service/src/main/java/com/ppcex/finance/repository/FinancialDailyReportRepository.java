package com.ppcex.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.finance.entity.FinancialDailyReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FinancialDailyReportRepository extends BaseMapper<FinancialDailyReport> {

    @Select("SELECT * FROM financial_daily_report WHERE report_date = #{reportDate}")
    FinancialDailyReport getReportByDate(@Param("reportDate") LocalDate reportDate);

    @Select("SELECT * FROM financial_daily_report WHERE report_date BETWEEN #{startDate} AND #{endDate} ORDER BY report_date DESC")
    List<FinancialDailyReport> getReportsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(total_trade_volume), 0) FROM financial_daily_report WHERE report_date BETWEEN #{startDate} AND #{endDate}")
    java.math.BigDecimal getTotalTradeVolumeByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(total_fee_income), 0) FROM financial_daily_report WHERE report_date BETWEEN #{startDate} AND #{endDate}")
    java.math.BigDecimal getTotalFeeIncomeByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(AVG(active_users), 0) FROM financial_daily_report WHERE report_date BETWEEN #{startDate} AND #{endDate}")
    java.math.BigDecimal getAvgActiveUsersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}