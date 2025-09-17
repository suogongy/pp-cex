package com.cex.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.trade.service.OrderService;
import com.cex.trade.dto.OrderCreateDTO;
import com.cex.trade.dto.OrderCancelDTO;
import com.cex.trade.dto.OrderVO;
import com.cex.common.response.Result;
import com.cex.common.response.PageResult;
import com.cex.common.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/api/trade/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        Long userId = UserContext.getCurrentUserId();
        OrderVO order = orderService.createOrder(userId, orderCreateDTO);
        return Result.success(order);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel")
    public Result<OrderVO> cancelOrder(@Valid @RequestBody OrderCancelDTO orderCancelDTO) {
        Long userId = UserContext.getCurrentUserId();
        OrderVO order = orderService.cancelOrder(userId, orderCancelDTO);
        return Result.success(order);
    }

    @Operation(summary = "根据订单编号获取订单")
    @GetMapping("/{orderNo}")
    public Result<OrderVO> getOrderByOrderNo(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        OrderVO order = orderService.getOrderByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> getUserOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "交易对") @RequestParam(required = false) String symbol,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "订单类型") @RequestParam(required = false) Integer orderType) {

        Long userId = UserContext.getCurrentUserId();
        Page<com.cex.trade.entity.TradeOrder> pageParam = new Page<>(page, size);
        IPage<OrderVO> result = orderService.getUserOrders(pageParam, userId, symbol, status, orderType);

        PageResult<OrderVO> pageResult = new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );

        return Result.success(pageResult);
    }

    @Operation(summary = "获取用户当前委托")
    @GetMapping("/active")
    public Result<List<OrderVO>> getUserActiveOrders() {
        Long userId = UserContext.getCurrentUserId();
        List<OrderVO> orders = orderService.getUserActiveOrders(userId);
        return Result.success(orders);
    }

    @Operation(summary = "获取交易对活跃订单")
    @GetMapping("/symbol/{symbol}/active")
    public Result<List<OrderVO>> getActiveOrdersBySymbol(
            @Parameter(description = "交易对") @PathVariable String symbol) {
        List<OrderVO> orders = orderService.getActiveOrdersBySymbol(symbol);
        return Result.success(orders);
    }
}