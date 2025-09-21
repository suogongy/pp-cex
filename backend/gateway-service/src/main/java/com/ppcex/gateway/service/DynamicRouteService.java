package com.ppcex.gateway.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteService {

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private final ApplicationEventPublisher publisher;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String ROUTE_CACHE_PREFIX = "gateway:route:";
    private static final String ROUTE_LIST_KEY = "gateway:routes:list";

    // 路由配置缓存
    private final Map<String, RouteDefinition> routeCache = new ConcurrentHashMap<>();

    /**
     * 添加路由
     */
    public Mono<Void> addRoute(RouteDefinition definition) {
        return Mono.fromRunnable(() -> {
            try {
                log.info("Adding route: {}", definition.getId());

                // 验证路由配置
                validateRouteDefinition(definition);

                // 检查路由是否已存在
                if (routeCache.containsKey(definition.getId())) {
                    throw new IllegalArgumentException("Route " + definition.getId() + " already exists");
                }

                // 添加路由到网关
                routeDefinitionWriter.save(Mono.just(definition)).subscribe();

                // 缓存路由配置
                cacheRouteDefinition(definition);

                // 刷新路由
                refreshRoutes();

                log.info("Route added successfully: {}", definition.getId());
            } catch (Exception e) {
                log.error("Failed to add route: {}", definition.getId(), e);
                throw new RuntimeException("Failed to add route: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 更新路由
     */
    public Mono<Void> updateRoute(RouteDefinition definition) {
        return Mono.fromRunnable(() -> {
            try {
                log.info("Updating route: {}", definition.getId());

                // 验证路由配置
                validateRouteDefinition(definition);

                // 检查路由是否存在
                if (!routeCache.containsKey(definition.getId())) {
                    throw new IllegalArgumentException("Route " + definition.getId() + " not found");
                }

                // 删除旧路由
                deleteRoute(definition.getId()).subscribe();

                // 添加新路由
                addRoute(definition).subscribe();

                log.info("Route updated successfully: {}", definition.getId());
            } catch (Exception e) {
                log.error("Failed to update route: {}", definition.getId(), e);
                throw new RuntimeException("Failed to update route: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 删除路由
     */
    public Mono<Void> deleteRoute(String routeId) {
        return Mono.fromRunnable(() -> {
            try {
                log.info("Deleting route: {}", routeId);

                // 从网关删除路由
                routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();

                // 从缓存删除路由
                removeRouteFromCache(routeId);

                // 刷新路由
                refreshRoutes();

                log.info("Route deleted successfully: {}", routeId);
            } catch (Exception e) {
                log.error("Failed to delete route: {}", routeId, e);
                throw new RuntimeException("Failed to delete route: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 获取所有路由
     */
    public Flux<RouteDefinition> getAllRoutes() {
        return routeDefinitionLocator.getRouteDefinitions()
                .doOnNext(route -> log.debug("Found route: {}", route.getId()))
                .doOnError(error -> log.error("Error getting routes", error));
    }

    /**
     * 根据ID获取路由
     */
    public Mono<RouteDefinition> getRouteById(String routeId) {
        return getAllRoutes()
                .filter(route -> routeId.equals(route.getId()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Route not found: " + routeId)));
    }

    /**
     * 获取路由统计信息
     */
    public Map<String, Object> getRouteStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<RouteDefinition> routes = getAllRoutes().collectList().block();
            if (routes != null) {
                stats.put("totalRoutes", routes.size());
                stats.put("activeRoutes", routes.stream().map(RouteDefinition::getId).collect(Collectors.toList()));

                // 按服务分组统计
                Map<String, Long> serviceStats = routes.stream()
                        .collect(Collectors.groupingBy(
                                route -> extractServiceName(route.getUri().toString()),
                                Collectors.counting()
                        ));
                stats.put("serviceDistribution", serviceStats);
            }
        } catch (Exception e) {
            log.error("Error getting route statistics", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * 批量添加路由
     */
    public Mono<Void> addRoutes(List<RouteDefinition> definitions) {
        if (CollectionUtils.isEmpty(definitions)) {
            return Mono.empty();
        }

        return Flux.fromIterable(definitions)
                .flatMap(this::addRoute)
                .then();
    }

    /**
     * 从Redis缓存加载路由
     */
    public void loadRoutesFromCache() {
        try {
            Object cached = stringRedisTemplate.opsForValue().get(ROUTE_LIST_KEY);
            if (cached instanceof String) {
                JSONArray routesArray = JSON.parseArray((String) cached);
                for (int i = 0; i < routesArray.size(); i++) {
                    JSONObject routeJson = routesArray.getJSONObject(i);
                    RouteDefinition definition = parseRouteDefinition(routeJson);
                    routeCache.put(definition.getId(), definition);
                }
                log.info("Loaded {} routes from cache", routeCache.size());
            }
        } catch (Exception e) {
            log.error("Failed to load routes from cache", e);
        }
    }

    /**
     * 保存路由到Redis缓存
     */
    public void saveRoutesToCache() {
        try {
            JSONArray routesArray = new JSONArray();
            routeCache.values().forEach(route -> {
                JSONObject routeJson = new JSONObject();
                routeJson.put("id", route.getId());
                routeJson.put("uri", route.getUri().toString());
                routeJson.put("predicates", route.getPredicates());
                routeJson.put("filters", route.getFilters());
                routeJson.put("order", route.getOrder());
                routeJson.put("metadata", route.getMetadata());
                routesArray.add(routeJson);
            });

            stringRedisTemplate.opsForValue().set(ROUTE_LIST_KEY, routesArray.toJSONString(),
                    Duration.ofMinutes(5));

            log.info("Saved {} routes to cache", routeCache.size());
        } catch (Exception e) {
            log.error("Failed to save routes to cache", e);
        }
    }

    /**
     * 验证路由配置
     */
    private void validateRouteDefinition(RouteDefinition definition) {
        if (definition.getId() == null || definition.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty");
        }

        if (definition.getUri() == null) {
            throw new IllegalArgumentException("Route URI cannot be null");
        }

        if (CollectionUtils.isEmpty(definition.getPredicates())) {
            throw new IllegalArgumentException("Route predicates cannot be empty");
        }

        // 验证URI格式
        String uriStr = definition.getUri().toString();
        if (!uriStr.startsWith("lb://") && !uriStr.startsWith("http://") && !uriStr.startsWith("https://")) {
            throw new IllegalArgumentException("Route URI must start with lb://, http:// or https://");
        }
    }

    /**
     * 缓存路由配置
     */
    private void cacheRouteDefinition(RouteDefinition definition) {
        routeCache.put(definition.getId(), definition);

        String routeKey = ROUTE_CACHE_PREFIX + definition.getId();
        stringRedisTemplate.opsForValue().set(routeKey, JSON.toJSONString(definition),
                Duration.ofMinutes(5));
    }

    /**
     * 从缓存删除路由
     */
    private void removeRouteFromCache(String routeId) {
        routeCache.remove(routeId);

        String routeKey = ROUTE_CACHE_PREFIX + routeId;
        stringRedisTemplate.delete(routeKey);
    }

    /**
     * 刷新路由
     */
    private void refreshRoutes() {
        publisher.publishEvent(new RefreshRoutesEvent(this));
        saveRoutesToCache();
    }

    /**
     * 解析路由定义
     */
    private RouteDefinition parseRouteDefinition(JSONObject routeJson) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(routeJson.getString("id"));
        definition.setUri(URI.create(routeJson.getString("uri")));
        definition.setOrder(routeJson.getIntValue("order"));

        // 解断言和过滤器
        // 这里简化处理，实际应该完整解析

        return definition;
    }

    /**
     * 提取服务名称
     */
    private String extractServiceName(String uri) {
        if (uri.startsWith("lb://")) {
            return uri.substring(5);
        }
        return "unknown";
    }

    /**
     * 获取缓存中的路由数量
     */
    public int getCachedRouteCount() {
        return routeCache.size();
    }

    /**
     * 清空路由缓存
     */
    public void clearRouteCache() {
        routeCache.clear();
        stringRedisTemplate.delete(ROUTE_LIST_KEY);

        // 清理所有路由键
        Set<String> keys = stringRedisTemplate.keys(ROUTE_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }

        log.info("Route cache cleared");
    }
}