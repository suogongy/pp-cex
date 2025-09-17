package com.ppcex.trade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeDetailVO {

    private Long id;

    private String tradeNo;

    private String symbol;

    private String pairName;

    private Long makerOrderId;

    private Long takerOrderId;

    private Long makerUserId;

    private Long takerUserId;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal value;

    private BigDecimal makerFee;

    private BigDecimal takerFee;

    private LocalDateTime createTime;
}