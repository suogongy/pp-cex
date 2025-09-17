package com.ppcex.match.controller;

import com.ppcex.match.engine.MatchingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    @Autowired
    private MatchingEngine matchingEngine;

    @MessageMapping("/subscribe/{symbol}")
    @SendTo("/topic/orderbook/{symbol}")
    public Map<String, Object> subscribeOrderBook(String symbol) {
        return matchingEngine.getOrderBookSnapshot(symbol);
    }

    @MessageMapping("/price/{symbol}")
    @SendTo("/topic/price/{symbol}")
    public Map<String, Object> getPriceUpdate(String symbol) {
        Map<String, Object> priceUpdate = Map.of(
            "symbol", symbol,
            "price", matchingEngine.getLatestPrice(symbol),
            "timestamp", System.currentTimeMillis()
        );
        return priceUpdate;
    }

    @MessageMapping("/trade/{symbol}")
    @SendTo("/topic/trade/{symbol}")
    public Map<String, Object> getTradeUpdate(String symbol) {
        Map<String, Object> snapshot = matchingEngine.getOrderBookSnapshot(symbol);
        return Map.of(
            "symbol", symbol,
            "trades", snapshot.get("recentTrades"),
            "timestamp", System.currentTimeMillis()
        );
    }
}