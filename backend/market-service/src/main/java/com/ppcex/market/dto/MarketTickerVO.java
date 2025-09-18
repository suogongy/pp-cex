package com.ppcex.market.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MarketTickerVO {

    private Long id;

    private String symbol;

    private String pairName;

    private BigDecimal lastPrice;

    private BigDecimal openPrice;

    private BigDecimal highPrice;

    private BigDecimal lowPrice;

    private BigDecimal volume;

    private BigDecimal quoteVolume;

    private BigDecimal priceChange;

    private BigDecimal priceChangePercent;

    private Integer count;

    private LocalDateTime lastUpdateTime;

    private String priceChangeType; // UP, DOWN, EQUAL

    private String priceChangePercentText; // +1.56%, -2.34%
}