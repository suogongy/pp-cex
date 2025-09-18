package com.ppcex.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("market_depth")
public class MarketDepth {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String symbol;

    private String bids;

    private String asks;

    private Long timestamp;

    private LocalDateTime createTime;
}