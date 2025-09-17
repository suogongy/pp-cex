package com.ppcex.match.service;

import com.ppcex.match.entity.MatchOrder;
import com.ppcex.match.entity.TradeRecord;

public interface TradeService {
    void processTrade(TradeRecord tradeRecord, MatchOrder buyOrder, MatchOrder sellOrder);
    void saveTradeRecord(TradeRecord tradeRecord);
    void updateOrderStatus(MatchOrder order);
    void notifyTradeUpdate(TradeRecord tradeRecord);
}