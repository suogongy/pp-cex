package com.cex.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.trade.entity.TradePair;
import com.cex.trade.service.TradePairService;
import com.cex.trade.dto.TradePairVO;
import com.cex.common.response.Result;
import com.cex.common.response.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "交易对管理", description = "交易对相关接口")
@RestController
@RequestMapping("/api/trade/pairs")
public class TradePairController {

    @Autowired
    private TradePairService tradePairService;

    @Operation(summary = "获取交易对列表")
    @GetMapping
    public Result<PageResult<TradePairVO>> getTradePairs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "交易对符号") @RequestParam(required = false) String symbol,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        Page<TradePair> pageParam = new Page<>(page, size);
        IPage<TradePairVO> result = tradePairService.getTradePairPage(pageParam, symbol, status);

        PageResult<TradePairVO> pageResult = new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );

        return Result.success(pageResult);
    }

    @Operation(summary = "获取活跃交易对列表")
    @GetMapping("/active")
    public Result<List<TradePairVO>> getActiveTradePairs() {
        List<TradePairVO> tradePairs = tradePairService.getActiveTradePairs();
        return Result.success(tradePairs);
    }

    @Operation(summary = "根据符号获取交易对")
    @GetMapping("/symbol/{symbol}")
    public Result<TradePairVO> getTradePairBySymbol(
            @Parameter(description = "交易对符号") @PathVariable String symbol) {
        TradePairVO tradePair = tradePairService.getTradePairBySymbol(symbol);
        if (tradePair == null) {
            return Result.error("交易对不存在");
        }
        return Result.success(tradePair);
    }

    @Operation(summary = "根据ID获取交易对")
    @GetMapping("/{id}")
    public Result<TradePairVO> getTradePairById(
            @Parameter(description = "交易对ID") @PathVariable Long id) {
        TradePairVO tradePair = tradePairService.getTradePairById(id);
        if (tradePair == null) {
            return Result.error("交易对不存在");
        }
        return Result.success(tradePair);
    }

    @Operation(summary = "添加交易对")
    @PostMapping
    public Result<Boolean> addTradePair(@Valid @RequestBody TradePair tradePair) {
        boolean success = tradePairService.addTradePair(tradePair);
        return success ? Result.success(true) : Result.error("添加交易对失败");
    }

    @Operation(summary = "更新交易对")
    @PutMapping("/{id}")
    public Result<Boolean> updateTradePair(
            @Parameter(description = "交易对ID") @PathVariable Long id,
            @Valid @RequestBody TradePair tradePair) {
        tradePair.setId(id);
        boolean success = tradePairService.updateTradePair(tradePair);
        return success ? Result.success(true) : Result.error("更新交易对失败");
    }

    @Operation(summary = "删除交易对")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTradePair(
            @Parameter(description = "交易对ID") @PathVariable Long id) {
        boolean success = tradePairService.deleteTradePair(id);
        return success ? Result.success(true) : Result.error("删除交易对失败");
    }
}