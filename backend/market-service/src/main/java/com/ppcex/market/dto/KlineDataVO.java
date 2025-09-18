package com.ppcex.market.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class KlineDataVO {

    private String symbol;

    private String interval;

    private List<Object[]> klines; // [open_time, open_price, high_price, low_price, close_price, volume, close_time, quote_volume, trades_count, taker_buy_base_volume, taker_buy_quote_volume, ignore]
}