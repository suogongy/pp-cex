package com.ppcex.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("market_ticker")
public class MarketTicker {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String symbol;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}