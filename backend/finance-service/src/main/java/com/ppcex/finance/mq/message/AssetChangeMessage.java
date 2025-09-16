package com.ppcex.finance.mq.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class AssetChangeMessage {

    private String messageId;

    private String timestamp;

    private Long userId;

    private String coinId;

    private String coinName;

    private Integer businessType;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private BigDecimal fee;

    private String flowNo;

    private String refOrderNo;

    private String refTxHash;

    private String remark;

    private String traceId;
}