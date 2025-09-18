package com.ppcex.match.controller;

import com.ppcex.common.response.Result;
import com.ppcex.match.dto.OrderBookRequest;
import com.ppcex.match.dto.OrderBookResponse;
import com.ppcex.match.dto.TradeHistoryRequest;
import com.ppcex.match.dto.TradeHistoryResponse;
import com.ppcex.match.engine.MatchingEngine;
import com.ppcex.match.entity.TradeRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/match")
@Tag(name = "撮合服务", description = "撮合引擎相关接口")
@RequiredArgsConstructor
public class MatchController {

    @Autowired
    private MatchingEngine matchingEngine;

    @PostMapping("/orderbook")
    @Operation(summary = "获取订单簿")
    public Result<OrderBookResponse> getOrderBook(@RequestBody OrderBookRequest request) {
        try {
            Map<String, Object> snapshot = matchingEngine.getOrderBookSnapshot(request.getSymbol());
            OrderBookResponse response = new OrderBookResponse();
            response.setSymbol(request.getSymbol());
            response.setSnapshot(snapshot);
            response.setTimestamp(System.currentTimeMillis());
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取订单簿失败: " + e.getMessage());
        }
    }

    @GetMapping("/price/{symbol}")
    @Operation(summary = "获取最新价格")
    public Result<BigDecimal> getLatestPrice(@PathVariable String symbol) {
        try {
            BigDecimal price = matchingEngine.getLatestPrice(symbol);
            return Result.success(price);
        } catch (Exception e) {
            return Result.error("获取最新价格失败: " + e.getMessage());
        }
    }

    @PostMapping("/trade/history")
    @Operation(summary = "获取交易历史")
    public Result<TradeHistoryResponse> getTradeHistory(@RequestBody TradeHistoryRequest request) {
        try {
            Map<String, Object> snapshot = matchingEngine.getOrderBookSnapshot(request.getSymbol());
            List<TradeRecord> trades = (List<TradeRecord>) snapshot.get("recentTrades");

            TradeHistoryResponse response = new TradeHistoryResponse();
            response.setSymbol(request.getSymbol());
            response.setTrades(trades);
            response.setTimestamp(System.currentTimeMillis());
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取交易历史失败: " + e.getMessage());
        }
    }

    @GetMapping("/symbols")
    @Operation(summary = "获取活跃交易对")
    public Result<List<String>> getActiveSymbols() {
        try {
            List<String> symbols = List.copyOf(matchingEngine.getActiveSymbols());
            return Result.success(symbols);
        } catch (Exception e) {
            return Result.error("获取活跃交易对失败: " + e.getMessage());
        }
    }

    @PostMapping("/clear/{symbol}")
    @Operation(summary = "清空交易对订单簿")
    public Result<String> clearOrderBook(@PathVariable String symbol) {
        try {
            matchingEngine.clearOrderBook(symbol);
            return Result.success("订单簿已清空: " + symbol);
        } catch (Exception e) {
            return Result.error("清空订单簿失败: " + e.getMessage());
        }
    }

    @PostMapping("/clear-all")
    @Operation(summary = "清空所有订单簿")
    public Result<String> clearAllOrderBooks() {
        try {
            matchingEngine.clearAllOrderBooks();
            return Result.success("所有订单簿已清空");
        } catch (Exception e) {
            return Result.error("清空所有订单簿失败: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<String> health() {
        return Result.success("撮合服务运行正常");
    }
}