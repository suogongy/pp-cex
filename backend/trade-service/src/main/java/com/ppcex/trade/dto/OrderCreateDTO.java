package com.ppcex.trade.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OrderCreateDTO {

    @NotBlank(message = "交易对不能为空")
    private String symbol;

    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @NotNull(message = "买卖方向不能为空")
    private Integer direction;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.00000001", message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.00000001", message = "数量必须大于0")
    private BigDecimal amount;

    private Integer timeInForce = 1;

    private Integer source = 1;
}