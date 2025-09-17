package com.ppcex.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单簿请求")
public class OrderBookRequest {
    @Schema(description = "交易对", required = true)
    private String symbol;

    @Schema(description = "深度级别")
    private Integer depth = 20;

    @Schema(description = "聚合精度")
    private Integer precision = 8;
}