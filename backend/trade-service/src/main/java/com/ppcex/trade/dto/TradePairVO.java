package com.ppcex.trade.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradePairVO {

    private Long id;

    private String symbol;

    private String baseCoin;

    private String quoteCoin;

    private String pairName;

    private Integer status;

    private Integer pricePrecision;

    private Integer amountPrecision;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal feeRate;

    private Integer sortOrder;
}