package com.ppcex.gateway.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态路由管理服务
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicRouteService {

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private final ApplicationEventPublisher publisher;

    @NacosValue(value = "${gateway.routes.enabled:true}", autoRefreshed = true)
    private boolean routesEnabled;

    @PostConstruct
    public void init() {
        log.info("Dynamic route service initialized, routes enabled: {}", routesEnabled);
    }

    /**
     * 监听Nacos路由配置变更
     */
    @NacosConfigListener(dataId = "gateway-routes.yaml", groupId = "gateway-service")
    public void onRouteConfigChange(String config) {
        try {
            log.info("Route config changed, updating routes...");

            List<RouteDefinition> routeDefinitions = parseRouteConfig(config);
            updateRoutes(routeDefinitions);

            log.info("Routes updated successfully, total routes: {}", routeDefinitions.size());
        } catch (Exception e) {
            log.error("Failed to update routes from config", e);
        }
    }

    /**
     * 解析路由配置
     */
    private List<RouteDefinition> parseRouteConfig(String config) {
        try {
            RouteConfig routeConfig = JSON.parseObject(config, RouteConfig.class);

            if (routeConfig == null || routeConfig.getRoutes() == null) {
                log.warn("Invalid route config: {}", config);
                return new ArrayList<>();
            }

            List<RouteDefinition> definitions = new ArrayList<>();

            for (RouteInfo routeInfo : routeConfig.getRoutes()) {
                if (Boolean.TRUE.equals(routeInfo.getEnabled())) {
                    RouteDefinition definition = buildRouteDefinition(routeInfo);
                    definitions.add(definition);
                }
            }

            return definitions;
        } catch (Exception e) {
            log.error("Failed to parse route config: {}", config, e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建路由定义
     */
    private RouteDefinition buildRouteDefinition(RouteInfo routeInfo) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(routeInfo.getId());
        definition.setUri(routeInfo.getUri());
        definition.setOrder(routeInfo.getOrder());
        definition.setMetadata(routeInfo.getMetadata());

        // 构建断言
        List<org.springframework.cloud.gateway.filter.FilterDefinition> predicates = new ArrayList<>();
        if (routeInfo.getPredicates() != null) {
            for (PredicateInfo predicateInfo : routeInfo.getPredicates()) {
                org.springframework.cloud.gateway.filter.FilterDefinition predicate =
                    new org.springframework.cloud.gateway.filter.FilterDefinition();
                predicate.setName(predicateInfo.getName());
                predicate.setArgs(predicateInfo.getArgs());
                predicates.add(predicate);
            }
        }
        definition.setPredicates(predicates);

        // 构建过滤器
        List<org.springframework.cloud.gateway.filter.FilterDefinition> filters = new ArrayList<>();
        if (routeInfo.getFilters() != null) {
            for (FilterInfo filterInfo : routeInfo.getFilters()) {
                org.springframework.cloud.gateway.filter.FilterDefinition filter =
                    new org.springframework.cloud.gateway.filter.FilterDefinition();
                filter.setName(filterInfo.getName());
                filter.setArgs(filterInfo.getArgs());
                filters.add(filter);
            }
        }
        definition.setFilters(filters);

        return definition;
    }

    /**
     * 更新路由
     */
    private void updateRoutes(List<RouteDefinition> routeDefinitions) {
        try {
            // 删除现有路由
            routeDefinitionLocator.getRouteDefinitions()
                .subscribe(route -> {
                    routeDefinitionWriter.delete(Mono.just(route.getId())).subscribe();
                });

            // 添加新路由
            for (RouteDefinition routeDefinition : routeDefinitions) {
                routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            }

            // 发布路由刷新事件
            publisher.publishEvent(new RefreshRoutesEvent(this));

            log.info("Routes updated: {} routes", routeDefinitions.size());
        } catch (Exception e) {
            log.error("Failed to update routes", e);
        }
    }

    /**
     * 获取所有路由
     */
    public List<RouteDefinition> getAllRoutes() {
        return routeDefinitionLocator.getRouteDefinitions().collectList().block();
    }

    /**
     * 刷新路由
     */
    public void refreshRoutes() {
        publisher.publishEvent(new RefreshRoutesEvent(this));
        log.info("Routes refreshed");
    }

    /**
     * 路由配置类
     */
    public static class RouteConfig {
        private List<RouteInfo> routes;

        public List<RouteInfo> getRoutes() {
            return routes;
        }

        public void setRoutes(List<RouteInfo> routes) {
            this.routes = routes;
        }
    }

    /**
     * 路由信息类
     */
    public static class RouteInfo {
        private String id;
        private String uri;
        private Integer order;
        private Boolean enabled;
        private List<PredicateInfo> predicates;
        private List<FilterInfo> filters;
        private java.util.Map<String, String> metadata;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public List<PredicateInfo> getPredicates() { return predicates; }
        public void setPredicates(List<PredicateInfo> predicates) { this.predicates = predicates; }
        public List<FilterInfo> getFilters() { return filters; }
        public void setFilters(List<FilterInfo> filters) { this.filters = filters; }
        public java.util.Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(java.util.Map<String, String> metadata) { this.metadata = metadata; }
    }

    /**
     * 断言信息类
     */
    public static class PredicateInfo {
        private String name;
        private java.util.Map<String, String> args;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public java.util.Map<String, String> getArgs() { return args; }
        public void setArgs(java.util.Map<String, String> args) { this.args = args; }
    }

    /**
     * 过滤器信息类
     */
    public static class FilterInfo {
        private String name;
        private java.util.Map<String, String> args;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public java.util.Map<String, String> getArgs() { return args; }
        public void setArgs(java.util.Map<String, String> args) { this.args = args; }
    }
}