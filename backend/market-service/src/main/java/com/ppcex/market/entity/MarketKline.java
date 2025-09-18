package com.ppcex.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("market_kline")
public class MarketKline {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String symbol;

    private String interval;

    private Long openTime;

    private Long closeTime;

    private BigDecimal openPrice;

    private BigDecimal highPrice;

    private BigDecimal lowPrice;

    private BigDecimal closePrice;

    private BigDecimal volume;

    private BigDecimal quoteVolume;

    private Integer tradesCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}