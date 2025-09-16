package com.ppcex.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RiskCheckResult {

    private Boolean pass;

    private Integer riskLevel;

    private String riskScore;

    private List<String> triggeredRules;

    private String actionRequired;

    private String message;

    public static RiskCheckResult pass() {
        return new RiskCheckResult()
                .setPass(true)
                .setRiskLevel(1)
                .setMessage("风控检查通过");
    }

    public static RiskCheckResult fail(Integer riskLevel, String message, List<String> triggeredRules) {
        return new RiskCheckResult()
                .setPass(false)
                .setRiskLevel(riskLevel)
                .setMessage(message)
                .setTriggeredRules(triggeredRules);
    }
}