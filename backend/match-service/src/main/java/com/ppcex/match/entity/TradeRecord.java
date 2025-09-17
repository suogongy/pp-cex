package com.ppcex.match.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TradeRecord {
    private Long id;
    private String tradeNo;
    private String symbol;
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