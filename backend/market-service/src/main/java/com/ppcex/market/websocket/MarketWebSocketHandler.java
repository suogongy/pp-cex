package com.ppcex.market.websocket;

import com.ppcex.market.service.MarketTickerService;
import com.ppcex.market.service.MarketKlineService;
import com.ppcex.market.service.MarketTradeService;
import com.ppcex.market.service.MarketDepthService;
import com.ppcex.market.dto.MarketTickerVO;
import com.ppcex.market.dto.MarketTradeVO;
import com.ppcex.market.dto.MarketDepthVO;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class MarketWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private MarketTickerService marketTickerService;

    @Autowired
    private MarketTradeService marketTradeService;

    @Autowired
    private MarketDepthService marketDepthService;

    // 存储所有活跃的WebSocket会话
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    // 存储订阅关系：symbol -> List<WebSocketSession>
    private final Map<String, List<WebSocketSession>> symbolSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket连接建立: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("收到WebSocket消息: {}", payload);

            // 解析消息
            WebSocketMessage wsMessage = JSON.parseObject(payload, WebSocketMessage.class);

            if ("subscribe".equals(wsMessage.getMethod())) {
                handleSubscribe(session, wsMessage);
            } else if ("unsubscribe".equals(wsMessage.getMethod())) {
                handleUnsubscribe(session, wsMessage);
            } else {
                sendError(session, "不支持的方法: " + wsMessage.getMethod());
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);

        // 从所有订阅中移除该会话
        symbolSubscriptions.forEach((symbol, sessionList) -> sessionList.remove(session));
        symbolSubscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        log.info("WebSocket连接关闭: {}", session.getId());
    }

    private void handleSubscribe(WebSocketSession session, WebSocketMessage message) {
        String symbol = message.getParams().getSymbol();
        String channel = message.getParams().getChannel();

        if (symbol == null || symbol.isEmpty()) {
            sendError(session, "交易对符号不能为空");
            return;
        }

        // 添加到订阅列表
        symbolSubscriptions.computeIfAbsent(symbol, k -> new CopyOnWriteArrayList<>()).add(session);

        // 发送确认消息
        WebSocketResponse response = new WebSocketResponse();
        response.setId(message.getId());
        response.setResult("success");
        sendMessage(session, response);

        // 立即推送当前数据
        pushCurrentData(session, symbol, channel);
    }

    private void handleUnsubscribe(WebSocketSession session, WebSocketMessage message) {
        String symbol = message.getParams().getSymbol();

        if (symbol != null) {
            List<WebSocketSession> sessionList = symbolSubscriptions.get(symbol);
            if (sessionList != null) {
                sessionList.remove(session);
                if (sessionList.isEmpty()) {
                    symbolSubscriptions.remove(symbol);
                }
            }
        }

        // 发送确认消息
        WebSocketResponse response = new WebSocketResponse();
        response.setId(message.getId());
        response.setResult("success");
        sendMessage(session, response);
    }

    private void pushCurrentData(WebSocketSession session, String symbol, String channel) {
        try {
            // 推送行情数据
            MarketTickerVO ticker = marketTickerService.getTickerBySymbol(symbol);
            if (ticker != null) {
                WebSocketResponse tickerResponse = new WebSocketResponse();
                tickerResponse.setMethod("ticker");
                tickerResponse.setParams(new Params(symbol, "ticker"));
                tickerResponse.setData(ticker);
                sendMessage(session, tickerResponse);
            }

            // 推送最新成交数据
            List<MarketTradeVO> trades = marketTradeService.getRecentTrades(symbol, 10);
            if (!trades.isEmpty()) {
                WebSocketResponse tradeResponse = new WebSocketResponse();
                tradeResponse.setMethod("trade");
                tradeResponse.setParams(new Params(symbol, "trade"));
                tradeResponse.setData(trades);
                sendMessage(session, tradeResponse);
            }

            // 推送深度数据
            MarketDepthVO depth = marketDepthService.getMarketDepth(symbol, 20);
            WebSocketResponse depthResponse = new WebSocketResponse();
            depthResponse.setMethod("depth");
            depthResponse.setParams(new Params(symbol, "depth"));
            depthResponse.setData(depth);
            sendMessage(session, depthResponse);

        } catch (Exception e) {
            log.error("推送当前数据失败: {}", symbol, e);
        }
    }

    // 定时推送行情更新
    @Scheduled(fixedRate = 1000) // 每秒推送一次
    public void pushTickerUpdates() {
        symbolSubscriptions.forEach((symbol, sessionList) -> {
            try {
                MarketTickerVO ticker = marketTickerService.getTickerBySymbol(symbol);
                if (ticker != null) {
                    WebSocketResponse response = new WebSocketResponse();
                    response.setMethod("ticker");
                    response.setParams(new Params(symbol, "ticker"));
                    response.setData(ticker);
                    broadcastToSessions(sessionList, response);
                }
            } catch (Exception e) {
                log.error("推送行情更新失败: {}", symbol, e);
            }
        });
    }

    // 定时推送最新成交
    @Scheduled(fixedRate = 2000) // 每2秒推送一次
    public void pushTradeUpdates() {
        symbolSubscriptions.forEach((symbol, sessionList) -> {
            try {
                List<MarketTradeVO> trades = marketTradeService.getRecentTrades(symbol, 5);
                if (!trades.isEmpty()) {
                    WebSocketResponse response = new WebSocketResponse();
                    response.setMethod("trade");
                    response.setParams(new Params(symbol, "trade"));
                    response.setData(trades);
                    broadcastToSessions(sessionList, response);
                }
            } catch (Exception e) {
                log.error("推送成交更新失败: {}", symbol, e);
            }
        });
    }

    // 定时推送深度更新
    @Scheduled(fixedRate = 500) // 每500ms推送一次
    public void pushDepthUpdates() {
        symbolSubscriptions.forEach((symbol, sessionList) -> {
            try {
                MarketDepthVO depth = marketDepthService.getMarketDepth(symbol, 20);
                WebSocketResponse response = new WebSocketResponse();
                response.setMethod("depth");
                response.setParams(new Params(symbol, "depth"));
                response.setData(depth);
                broadcastToSessions(sessionList, response);
            } catch (Exception e) {
                log.error("推送深度更新失败: {}", symbol, e);
            }
        });
    }

    private void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    private void broadcastToSessions(List<WebSocketSession> sessions, Object message) {
        String jsonMessage = JSON.toJSONString(message);
        TextMessage textMessage = new TextMessage(jsonMessage);

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("广播WebSocket消息失败", e);
            }
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        WebSocketResponse response = new WebSocketResponse();
        response.setError(errorMessage);
        sendMessage(session, response);
    }

    // WebSocket消息类
    public static class WebSocketMessage {
        private String id;
        private String method;
        private Params params;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public Params getParams() { return params; }
        public void setParams(Params params) { this.params = params; }
    }

    public static class Params {
        private String symbol;
        private String channel;

        public Params() {}

        public Params(String symbol, String channel) {
            this.symbol = symbol;
            this.channel = channel;
        }

        // Getters and Setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
    }

    public static class WebSocketResponse {
        private String id;
        private String method;
        private Params params;
        private Object data;
        private String error;
        private String result;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public Params getParams() { return params; }
        public void setParams(Params params) { this.params = params; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }
}