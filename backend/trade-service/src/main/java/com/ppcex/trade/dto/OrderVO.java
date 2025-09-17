package com.ppcex.trade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long userId;

    private String symbol;

    private String pairName;

    private Integer orderType;

    private Integer direction;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal executedAmount;

    private BigDecimal executedValue;

    private BigDecimal fee;

    private Integer status;

    private String statusDesc;

    private Integer timeInForce;

    private Integer source;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime cancelTime;

    private LocalDateTime expireTime;
}