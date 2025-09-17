package com.ppcex.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private String symbol;

    private Integer orderType;

    private Integer direction;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal executedAmount;

    private BigDecimal executedValue;

    private BigDecimal fee;

    private Integer status;

    private Integer timeInForce;

    private Integer source;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime cancelTime;

    private LocalDateTime expireTime;
}