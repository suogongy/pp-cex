package com.cex.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.trade.service.TradeDetailService;
import com.cex.trade.engine.MatchingEngine;
import com.cex.trade.dto.TradeDetailVO;
import com.cex.common.response.Result;
import com.cex.common.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "交易接口", description = "交易相关接口")
@RestController
@RequestMapping("/api/trade")
public class TradeController {

    @Autowired
    private TradeDetailService tradeDetailService;

    @Autowired
    private MatchingEngine matchingEngine;

    @Operation(summary = "获取成交记录")
    @GetMapping("/trades")
    public Result<PageResult<TradeDetailVO>> getTrades(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "交易对") @RequestParam(required = false) String symbol,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {

        Long userId = null;
        Page<com.cex.trade.entity.TradeDetail> pageParam = new Page<>(page, size);
        IPage<TradeDetailVO> result = tradeDetailService.getTradeDetailPage(pageParam, userId, symbol, startTime, endTime);

        PageResult<TradeDetailVO> pageResult = new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );

        return Result.success(pageResult);
    }

    @Operation(summary = "获取最近成交记录")
    @GetMapping("/trades/recent")
    public Result<List<TradeDetailVO>> getRecentTrades(
            @Parameter(description = "交易对") @RequestParam String symbol,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "50") Integer limit) {
        List<TradeDetailVO> trades = tradeDetailService.getRecentTradesBySymbol(symbol, limit);
        return Result.success(trades);
    }

    @Operation(summary = "获取用户成交记录")
    @GetMapping("/trades/user")
    public Result<List<TradeDetailVO>> getUserTrades(
            @Parameter(description = "交易对") @RequestParam(required = false) String symbol) {
        Long userId = com.cex.common.util.UserContext.getCurrentUserId();
        List<TradeDetailVO> trades = tradeDetailService.getUserTrades(userId, symbol);
        return Result.success(trades);
    }

    @Operation(summary = "获取订单簿")
    @GetMapping("/orderbook/{symbol}")
    public Result<Map<String, Object>> getOrderBook(
            @Parameter(description = "交易对") @PathVariable String symbol) {
        Map<String, Object> orderBook = matchingEngine.getOrderBook(symbol);
        if (orderBook == null) {
            return Result.error("订单簿不存在");
        }
        return Result.success(orderBook);
    }

    @Operation(summary = "获取最新成交价格")
    @GetMapping("/price/{symbol}")
    public Result<BigDecimal> getLatestTradePrice(
            @Parameter(description = "交易对") @PathVariable String symbol) {
        BigDecimal price = matchingEngine.getLatestTradePrice(symbol);
        if (price == null) {
            return Result.error("暂无成交价格");
        }
        return Result.success(price);
    }

    @Operation(summary = "初始化订单簿")
    @PostMapping("/orderbook/{symbol}/init")
    public Result<String> initializeOrderBook(
            @Parameter(description = "交易对") @PathVariable String symbol) {
        matchingEngine.initializeOrderBook(symbol);
        return Result.success("订单簿初始化成功");
    }
}