package com.ppcex.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_detail")
public class TradeDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tradeNo;

    private String symbol;

    private Long makerOrderId;

    private Long takerOrderId;

    private Long makerUserId;

    private Long takerUserId;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal value;

    private BigDecimal makerFee;

    private BigDecimal takerFee;

    private LocalDateTime createTime;
}