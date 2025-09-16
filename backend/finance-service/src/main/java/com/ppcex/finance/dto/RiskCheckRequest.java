package com.ppcex.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class RiskCheckRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String clientIp;

    private String deviceInfo;

    private String location;

    private BigDecimal amount;

    private String coinId;

    private Integer businessType;

    private String operationType;

    private String userAgent;

    private String sessionId;

    private String orderNo;

    private String additionalData;
}