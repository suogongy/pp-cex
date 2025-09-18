package com.ppcex.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("market_trade")
public class MarketTrade {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tradeId;

    private String symbol;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal quoteVolume;

    private Long timestamp;

    private Integer isBuyerMaker;

    private LocalDateTime createTime;
}