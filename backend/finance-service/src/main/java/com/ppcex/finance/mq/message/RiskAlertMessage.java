package com.ppcex.finance.mq.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class RiskAlertMessage {

    private String messageId;

    private String timestamp;

    private Long userId;

    private String clientIp;

    private String deviceInfo;

    private String location;

    private BigDecimal amount;

    private String coinId;

    private Integer businessType;

    private Integer riskLevel;

    private List<String> triggeredRules;

    private String actionRequired;

    private String traceId;

    private String eventNo;
}