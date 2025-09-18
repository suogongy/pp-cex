package com.ppcex.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("market_pair")
public class MarketPair {

    @TableId(type = IdType.AUTO)
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}