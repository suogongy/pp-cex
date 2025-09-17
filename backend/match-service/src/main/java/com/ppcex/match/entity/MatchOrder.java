package com.ppcex.match.entity;

import com.ppcex.match.enums.DirectionEnum;
import com.ppcex.match.enums.OrderStatusEnum;
import com.ppcex.match.enums.OrderTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class MatchOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private String symbol;
    private OrderTypeEnum orderType;
    private DirectionEnum direction;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal executedAmount;
    private BigDecimal executedValue;
    private BigDecimal fee;
    private OrderStatusEnum status;
    private Integer timeInForce;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public boolean isActive() {
        return status == OrderStatusEnum.PENDING || status == OrderStatusEnum.PARTIALLY_FILLED;
    }

    public boolean isFullyFilled() {
        return status == OrderStatusEnum.FULLY_FILLED;
    }

    public BigDecimal getRemainingAmount() {
        return amount.subtract(executedAmount != null ? executedAmount : BigDecimal.ZERO);
    }
}