package com.ppcex.market.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppcex.market.entity.MarketPair;
import com.ppcex.market.service.MarketPairService;
import com.ppcex.market.dto.MarketPairVO;
import com.ppcex.common.response.Result;
import com.ppcex.common.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "市场交易对管理", description = "市场交易对相关接口")
@RestController
@RequestMapping("/api/v1/pairs")
public class MarketPairController {

    @Autowired
    private MarketPairService marketPairService;

    @Operation(summary = "获取交易对列表")
    @GetMapping
    public Result<PageResult<MarketPairVO>> getMarketPairs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "交易对符号") @RequestParam(required = false) String symbol,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        Page<MarketPair> pageParam = new Page<>(page, size);
        IPage<MarketPairVO> result = marketPairService.getMarketPairPage(pageParam, symbol, status);

        PageResult<MarketPairVO> pageResult = PageResult.of(
                result.getRecords(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        );

        return Result.success(pageResult);
    }

    @Operation(summary = "获取活跃交易对列表")
    @GetMapping("/active")
    public Result<List<MarketPairVO>> getActiveMarketPairs() {
        List<MarketPairVO> marketPairs = marketPairService.getActiveMarketPairs();
        return Result.success(marketPairs);
    }

    @Operation(summary = "根据符号获取交易对")
    @GetMapping("/symbol/{symbol}")
    public Result<MarketPairVO> getMarketPairBySymbol(
            @Parameter(description = "交易对符号") @PathVariable String symbol) {
        MarketPairVO marketPair = marketPairService.getMarketPairBySymbol(symbol);
        if (marketPair == null) {
            return Result.error("交易对不存在");
        }
        return Result.success(marketPair);
    }

    @Operation(summary = "根据ID获取交易对")
    @GetMapping("/{id}")
    public Result<MarketPairVO> getMarketPairById(
            @Parameter(description = "交易对ID") @PathVariable Long id) {
        MarketPairVO marketPair = marketPairService.getMarketPairById(id);
        if (marketPair == null) {
            return Result.error("交易对不存在");
        }
        return Result.success(marketPair);
    }

    @Operation(summary = "添加交易对")
    @PostMapping
    public Result<Boolean> addMarketPair(@Valid @RequestBody MarketPair marketPair) {
        boolean success = marketPairService.addMarketPair(marketPair);
        return success ? Result.success(true) : Result.error("添加交易对失败");
    }

    @Operation(summary = "更新交易对")
    @PutMapping("/{id}")
    public Result<Boolean> updateMarketPair(
            @Parameter(description = "交易对ID") @PathVariable Long id,
            @Valid @RequestBody MarketPair marketPair) {
        marketPair.setId(id);
        boolean success = marketPairService.updateMarketPair(marketPair);
        return success ? Result.success(true) : Result.error("更新交易对失败");
    }

    @Operation(summary = "删除交易对")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMarketPair(
            @Parameter(description = "交易对ID") @PathVariable Long id) {
        boolean success = marketPairService.deleteMarketPair(id);
        return success ? Result.success(true) : Result.error("删除交易对失败");
    }

    @Operation(summary = "同步交易对数据")
    @PostMapping("/sync")
    public Result<Boolean> syncMarketPairs() {
        boolean success = marketPairService.syncFromTradeService();
        return success ? Result.success(true) : Result.error("同步交易对失败");
    }

    @Operation(summary = "获取所有交易对")
    @GetMapping("/all")
    public Result<List<MarketPairVO>> getAllMarketPairs() {
        List<MarketPairVO> marketPairs = marketPairService.getAllMarketPairs();
        return Result.success(marketPairs);
    }
}