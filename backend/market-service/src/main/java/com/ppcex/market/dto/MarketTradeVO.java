package com.ppcex.market.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketTradeVO {

    private Long id;

    private String tradeId;

    private String symbol;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal quoteVolume;

    private Long timestamp;

    private Boolean isBuyerMaker;

    private String tradeType; // BUY, SELL
}