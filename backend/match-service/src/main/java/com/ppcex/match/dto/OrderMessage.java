package com.ppcex.match.dto;

import lombok.Data;

@Data
public class OrderMessage {
    private String action;
    private String orderNo;
    private Long userId;
    private String symbol;
    private String orderType;
    private String direction;
    private String price;
    private String amount;
    private Integer timeInForce;
}