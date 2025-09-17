package com.ppcex.trade.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class OrderCancelDTO {

    @NotBlank(message = "订单编号不能为空")
    private String orderNo;
}