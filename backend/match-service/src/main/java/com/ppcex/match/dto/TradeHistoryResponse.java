package com.ppcex.match.dto;

import com.ppcex.match.entity.TradeRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "交易历史响应")
public class TradeHistoryResponse {
    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "交易记录列表")
    private List<TradeRecord> trades;

    @Schema(description = "时间戳")
    private Long timestamp;
}