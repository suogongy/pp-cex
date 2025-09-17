package com.cex.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cex.trade.entity.TradeOrder;
import com.cex.trade.dto.OrderCreateDTO;
import com.cex.trade.dto.OrderCancelDTO;
import com.cex.trade.dto.OrderVO;

import java.util.List;

public interface OrderService extends IService<TradeOrder> {

    OrderVO createOrder(Long userId, OrderCreateDTO orderCreateDTO);

    OrderVO cancelOrder(Long userId, OrderCancelDTO orderCancelDTO);

    OrderVO getOrderByOrderNo(String orderNo);

    IPage<OrderVO> getUserOrders(Page<TradeOrder> page, Long userId, String symbol, Integer status, Integer orderType);

    List<OrderVO> getUserActiveOrders(Long userId);

    List<OrderVO> getActiveOrdersBySymbol(String symbol);

    boolean updateOrderStatus(Long orderId, Integer status);

    boolean updateOrderExecution(Long orderId, java.math.BigDecimal executedAmount, java.math.BigDecimal executedValue, java.math.BigDecimal fee);

    String generateOrderNo();
}