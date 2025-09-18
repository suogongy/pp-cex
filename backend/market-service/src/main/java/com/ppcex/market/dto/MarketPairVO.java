package com.ppcex.market.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketPairVO {

    private Long id;

    private String symbol;

    private String baseCoin;

    private String quoteCoin;

    private String pairName;

    private Integer status;

    private String statusName;

    private Integer pricePrecision;

    private Integer amountPrecision;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal feeRate;

    private Integer sortOrder;
}