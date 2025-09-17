package com.ppcex.match.service;

import com.ppcex.match.entity.MatchOrder;

import java.util.List;

public interface OrderService {
    void processOrder(MatchOrder order);
    void cancelOrder(String orderNo);
    MatchOrder getOrder(String orderNo);
    List<MatchOrder> getUserOrders(Long userId);
    List<MatchOrder> getSymbolOrders(String symbol);
}