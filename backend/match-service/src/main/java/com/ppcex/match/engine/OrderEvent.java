package com.ppcex.match.engine;

import com.ppcex.match.entity.MatchOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private MatchOrder order;
    private OrderEventType type;

    public enum OrderEventType {
        NEW_ORDER,
        CANCEL_ORDER,
        MODIFY_ORDER
    }
}