package com.ppcex.market.controller;

import com.ppcex.market.service.MarketTickerService;
import com.ppcex.market.service.MarketKlineService;
import com.ppcex.market.service.MarketTradeService;
import com.ppcex.market.service.MarketDepthService;
import com.ppcex.market.dto.MarketTickerVO;
import com.ppcex.market.dto.KlineDataVO;
import com.ppcex.market.dto.MarketTradeVO;
import com.ppcex.market.dto.MarketDepthVO;
import com.ppcex.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "市场行情", description = "市场行情数据接口")
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    @Autowired
    private MarketTickerService marketTickerService;

    @Autowired
    private MarketKlineService marketKlineService;

    @Autowired
    private MarketTradeService marketTradeService;

    @Autowired
    private MarketDepthService marketDepthService;

    @Operation(summary = "获取指定交易对的行情数据")
    @GetMapping("/ticker/{symbol}")
    public Result<MarketTickerVO> getTicker(
            @Parameter(description = "交易对符号") @PathVariable String symbol) {
        MarketTickerVO ticker = marketTickerService.getTickerBySymbol(symbol);
        if (ticker == null) {
            return Result.error("交易对不存在");
        }
        return Result.success(ticker);
    }

    @Operation(summary = "获取所有交易对的行情数据")
    @GetMapping("/tickers")
    public Result<List<MarketTickerVO>> getAllTickers() {
        List<MarketTickerVO> tickers = marketTickerService.getAllTickers();
        return Result.success(tickers);
    }

    @Operation(summary = "获取涨幅榜")
    @GetMapping("/top/gainers")
    public Result<List<MarketTickerVO>> getTopGainers(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<MarketTickerVO> gainers = marketTickerService.getTopGainers(limit);
        return Result.success(gainers);
    }

    @Operation(summary = "获取跌幅榜")
    @GetMapping("/top/losers")
    public Result<List<MarketTickerVO>> getTopLosers(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<MarketTickerVO> losers = marketTickerService.getTopLosers(limit);
        return Result.success(losers);
    }

    @Operation(summary = "获取成交量榜")
    @GetMapping("/top/volume")
    public Result<List<MarketTickerVO>> getTopVolume(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        List<MarketTickerVO> volume = marketTickerService.getTopVolume(limit);
        return Result.success(volume);
    }

    @Operation(summary = "获取K线数据")
    @GetMapping("/klines")
    public Result<List<Object[]>> getKlines(
            @Parameter(description = "交易对符号") @RequestParam String symbol,
            @Parameter(description = "时间间隔") @RequestParam String interval,
            @Parameter(description = "开始时间") @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) Long endTime,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "500") Integer limit) {

        List<Object[]> klines = marketKlineService.getKlineData(symbol, interval, startTime, endTime, limit)
                .stream()
                .map(kline -> new Object[]{
                        kline.getOpenTime(),
                        kline.getOpenPrice(),
                        kline.getHighPrice(),
                        kline.getLowPrice(),
                        kline.getClosePrice(),
                        kline.getVolume(),
                        kline.getCloseTime(),
                        kline.getQuoteVolume(),
                        kline.getTradesCount(),
                        0, // taker_buy_base_volume
                        0, // taker_buy_quote_volume
                        0  // ignore
                })
                .toList();

        return Result.success(klines);
    }

    @Operation(summary = "获取最新成交明细")
    @GetMapping("/trades")
    public Result<List<MarketTradeVO>> getRecentTrades(
            @Parameter(description = "交易对符号") @RequestParam String symbol,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "50") Integer limit) {

        List<MarketTradeVO> trades = marketTradeService.getRecentTrades(symbol, limit);
        return Result.success(trades);
    }

    @Operation(summary = "获取深度数据")
    @GetMapping("/depth")
    public Result<MarketDepthVO> getMarketDepth(
            @Parameter(description = "交易对符号") @RequestParam String symbol,
            @Parameter(description = "深度限制") @RequestParam(defaultValue = "20") Integer limit) {

        MarketDepthVO depth = marketDepthService.getMarketDepth(symbol, limit);
        return Result.success(depth);
    }

    @Operation(summary = "更新行情数据")
    @PostMapping("/ticker/update")
    public Result<Boolean> updateTicker(
            @Parameter(description = "交易对符号") @RequestParam String symbol,
            @Parameter(description = "最新价格") @RequestParam BigDecimal lastPrice,
            @Parameter(description = "最高价格") @RequestParam BigDecimal highPrice,
            @Parameter(description = "最低价格") @RequestParam BigDecimal lowPrice,
            @Parameter(description = "成交量") @RequestParam BigDecimal volume,
            @Parameter(description = "成交额") @RequestParam BigDecimal quoteVolume,
            @Parameter(description = "成交次数") @RequestParam Integer count) {

        marketTickerService.updateTicker(symbol, lastPrice, highPrice, lowPrice, volume, quoteVolume, count);
        return Result.success(true);
    }

    @Operation(summary = "添加成交记录")
    @PostMapping("/trade/add")
    public Result<Boolean> addTrade(
            @Parameter(description = "交易对符号") @RequestParam String symbol,
            @Parameter(description = "成交价格") @RequestParam BigDecimal price,
            @Parameter(description = "成交数量") @RequestParam BigDecimal amount,
            @Parameter(description = "时间戳") @RequestParam(required = false) Long timestamp,
            @Parameter(description = "是否买方挂单") @RequestParam(required = false) Integer isBuyerMaker) {

        marketTradeService.addTrade(null, symbol, price, amount, timestamp, isBuyerMaker);
        return Result.success(true);
    }

    @Operation(summary = "更新深度数据")
    @PostMapping("/depth/update")
    public Result<Boolean> updateDepth(
            @Parameter(description = "交易对符号") @RequestParam String symbol,
            @Parameter(description = "买单数据") @RequestParam List<BigDecimal[]> bids,
            @Parameter(description = "卖单数据") @RequestParam List<BigDecimal[]> asks,
            @Parameter(description = "时间戳") @RequestParam(required = false) Long timestamp) {

        marketDepthService.updateMarketDepth(symbol, bids, asks, timestamp);
        return Result.success(true);
    }
}