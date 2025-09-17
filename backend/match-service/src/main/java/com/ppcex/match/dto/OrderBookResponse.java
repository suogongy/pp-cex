package com.ppcex.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "订单簿响应")
public class OrderBookResponse {
    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "订单簿快照")
    private Map<String, Object> snapshot;

    @Schema(description = "时间戳")
    private Long timestamp;
}