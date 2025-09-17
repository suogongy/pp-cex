package com.ppcex.match.engine;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.ppcex.match.entity.MatchOrder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class DisruptorOrderProcessor {

    @Autowired
    private MatchingEngine matchingEngine;

    private Disruptor<OrderEvent> disruptor;
    private RingBuffer<OrderEvent> ringBuffer;

    @PostConstruct
    public void init() {
        disruptor = new Disruptor<>(
                new OrderEventFactory(),
                1024,
                Executors.defaultThreadFactory(),
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(new OrderEventHandler(matchingEngine));
        disruptor.start();

        ringBuffer = disruptor.getRingBuffer();
        log.info("Disruptor订单处理器初始化完成");
    }

    public void publishOrderEvent(MatchOrder order, OrderEvent.OrderEventType type) {
        long sequence = ringBuffer.next();
        try {
            OrderEvent event = ringBuffer.get(sequence);
            event.setOrder(order);
            event.setType(type);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
            log.info("Disruptor订单处理器已关闭");
        }
    }
}