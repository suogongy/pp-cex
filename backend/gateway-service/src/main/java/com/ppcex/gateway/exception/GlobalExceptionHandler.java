package com.ppcex.gateway.exception;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(-1)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>();

        if (ex instanceof GatewayException) {
            GatewayException gatewayException = (GatewayException) ex;
            result.put("code", gatewayException.getCode());
            result.put("message", gatewayException.getMessage());
            response.setRawStatusCode(gatewayException.getCode());
        } else {
            result.put("code", 500);
            result.put("message", "Internal Server Error");
            response.setRawStatusCode(500);
        }

        result.put("timestamp", System.currentTimeMillis());
        result.put("path", exchange.getRequest().getPath().value());

        log.error("Gateway Error: {} - {}", exchange.getRequest().getPath(), ex.getMessage(), ex);

        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(JSON.toJSONBytes(result));

        return response.writeWith(Mono.just(buffer));
    }
}