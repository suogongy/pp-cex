package com.ppcex.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "交易历史请求")
public class TradeHistoryRequest {
    @Schema(description = "交易对", required = true)
    private String symbol;

    @Schema(description = "限制数量")
    private Integer limit = 100;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;
}