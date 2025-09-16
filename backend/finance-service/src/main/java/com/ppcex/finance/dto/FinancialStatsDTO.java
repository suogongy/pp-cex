package com.ppcex.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class FinancialStatsDTO {

    private BigDecimal totalTradeVolume;

    private BigDecimal totalFeeIncome;

    private BigDecimal avgActiveUsers;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long reportCount;

    private BigDecimal growthRate;
}