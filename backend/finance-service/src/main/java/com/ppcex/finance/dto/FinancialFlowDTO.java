package com.ppcex.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class FinancialFlowDTO {

    @NotBlank(message = "流水编号不能为空")
    private String flowNo;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "币种ID不能为空")
    private String coinId;

    @NotBlank(message = "币种名称不能为空")
    private String coinName;

    @NotNull(message = "业务类型不能为空")
    private Integer businessType;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.00000001", message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "操作前余额不能为空")
    private BigDecimal balanceBefore;

    @NotNull(message = "操作后余额不能为空")
    private BigDecimal balanceAfter;

    @NotNull(message = "手续费不能为空")
    @DecimalMin(value = "0", message = "手续费不能为负数")
    private BigDecimal fee;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;

    private String refOrderNo;

    private String refTxHash;
}