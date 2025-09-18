package com.ppcex.market.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MarketDepthVO {

    private String symbol;

    private Long timestamp;

    private List<BigDecimal[]> bids; // [[price, quantity], ...]

    private List<BigDecimal[]> asks; // [[price, quantity], ...]

    private Long lastUpdateId;
}