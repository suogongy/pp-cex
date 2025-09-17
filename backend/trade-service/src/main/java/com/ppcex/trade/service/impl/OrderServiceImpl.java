package com.ppcex.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.trade.entity.TradeOrder;
import com.ppcex.trade.mapper.TradeOrderMapper;
import com.ppcex.trade.service.OrderService;
import com.ppcex.trade.service.TradePairService;
import com.ppcex.trade.dto.OrderCreateDTO;
import com.ppcex.trade.dto.OrderCancelDTO;
import com.ppcex.trade.dto.OrderVO;
import com.ppcex.trade.dto.TradePairVO;
import com.ppcex.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements OrderService {

    @Autowired
    private TradePairService tradePairService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_LOCK_PREFIX = "order:lock:";
    private static final String ORDER_NO_PREFIX = "ORD";
    private static final String ORDER_RATE_LIMIT_PREFIX = "order:rate:";

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        try {
            String lockKey = ORDER_LOCK_PREFIX + userId;
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(locked)) {
                throw new BusinessException("订单创建过于频繁，请稍后再试");
            }

            if (!checkRateLimit(userId)) {
                throw new BusinessException("订单创建频率超限，请稍后再试");
            }

            TradePairVO tradePair = tradePairService.getTradePairBySymbol(orderCreateDTO.getSymbol());
            if (tradePair == null) {
                throw new BusinessException("交易对不存在");
            }
            if (tradePair.getStatus() != 1) {
                throw new BusinessException("交易对已暂停");
            }

            validateOrder(orderCreateDTO, tradePair);

            TradeOrder order = new TradeOrder();
            BeanUtils.copyProperties(orderCreateDTO, order);
            order.setUserId(userId);
            order.setOrderNo(generateOrderNo());
            order.setStatus(1);
            order.setExecutedAmount(BigDecimal.ZERO);
            order.setExecutedValue(BigDecimal.ZERO);
            order.setFee(BigDecimal.ZERO);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            if (order.getTimeInForce() == 3) {
                order.setExpireTime(LocalDateTime.now().plusMinutes(5));
            }

            save(order);

            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setPairName(tradePair.getPairName());
            orderVO.setStatusDesc(getOrderStatusDesc(order.getStatus()));

            return orderVO;
        } finally {
            String lockKey = ORDER_LOCK_PREFIX + userId;
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public OrderVO cancelOrder(Long userId, OrderCancelDTO orderCancelDTO) {
        TradeOrder order = getOne(new QueryWrapper<TradeOrder>()
                .eq("order_no", orderCancelDTO.getOrderNo())
                .eq("user_id", userId));

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (order.getStatus() == 4) {
            throw new BusinessException("订单已取消");
        }

        if (order.getStatus() == 3) {
            throw new BusinessException("订单已完成，不可取消");
        }

        order.setStatus(4);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setStatusDesc(getOrderStatusDesc(order.getStatus()));

        return orderVO;
    }

    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        return baseMapper.selectOrderVOByOrderNo(orderNo);
    }

    @Override
    public IPage<OrderVO> getUserOrders(Page<TradeOrder> page, Long userId, String symbol, Integer status, Integer orderType) {
        return baseMapper.selectOrderVOPage(page, userId, symbol, status, orderType);
    }

    @Override
    public List<OrderVO> getUserActiveOrders(Long userId) {
        return baseMapper.selectUserActiveOrders(userId);
    }

    @Override
    public List<OrderVO> getActiveOrdersBySymbol(String symbol) {
        return baseMapper.selectActiveOrdersBySymbol(symbol);
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        TradeOrder order = new TradeOrder();
        order.setId(orderId);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean updateOrderExecution(Long orderId, BigDecimal executedAmount, BigDecimal executedValue, BigDecimal fee) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            return false;
        }

        order.setExecutedAmount(order.getExecutedAmount().add(executedAmount));
        order.setExecutedValue(order.getExecutedValue().add(executedValue));
        order.setFee(order.getFee().add(fee));
        order.setUpdateTime(LocalDateTime.now());

        if (order.getExecutedAmount().compareTo(order.getAmount()) >= 0) {
            order.setStatus(3);
        } else {
            order.setStatus(2);
        }

        return updateById(order);
    }

    @Override
    public String generateOrderNo() {
        return ORDER_NO_PREFIX + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    private void validateOrder(OrderCreateDTO orderCreateDTO, TradePairVO tradePair) {
        if (orderCreateDTO.getAmount().compareTo(tradePair.getMinAmount()) < 0) {
            throw new BusinessException("订单数量不能小于最小数量 " + tradePair.getMinAmount());
        }
        if (orderCreateDTO.getAmount().compareTo(tradePair.getMaxAmount()) > 0) {
            throw new BusinessException("订单数量不能大于最大数量 " + tradePair.getMaxAmount());
        }
        if (orderCreateDTO.getPrice().compareTo(tradePair.getMinPrice()) < 0) {
            throw new BusinessException("订单价格不能小于最小价格 " + tradePair.getMinPrice());
        }
        if (orderCreateDTO.getPrice().compareTo(tradePair.getMaxPrice()) > 0) {
            throw new BusinessException("订单价格不能大于最大价格 " + tradePair.getMaxPrice());
        }

        int amountScale = orderCreateDTO.getAmount().scale();
        if (amountScale > tradePair.getAmountPrecision()) {
            throw new IllegalArgumentException("数量精度不能超过 " + tradePair.getAmountPrecision() + " 位");
        }

        int priceScale = orderCreateDTO.getPrice().scale();
        if (priceScale > tradePair.getPricePrecision()) {
            throw new IllegalArgumentException("价格精度不能超过 " + tradePair.getPricePrecision() + " 位");
        }
    }

    private boolean checkRateLimit(Long userId) {
        String key = ORDER_RATE_LIMIT_PREFIX + userId;
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        if (count == null) {
            redisTemplate.opsForValue().set(key, 1, 1, TimeUnit.MINUTES);
            return true;
        } else if (count < 10) {
            redisTemplate.opsForValue().increment(key);
            return true;
        } else {
            return false;
        }
    }

    private String getOrderStatusDesc(Integer status) {
        switch (status) {
            case 1: return "待成交";
            case 2: return "部分成交";
            case 3: return "完全成交";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}