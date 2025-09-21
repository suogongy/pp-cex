package com.ppcex.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckService {

    private final DiscoveryClient discoveryClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    private static final String HEALTH_CACHE_PREFIX = "health:service:";
    private static final String SERVICE_STATUS_PREFIX = "service:status:";

    // 服务健康状态缓存
    private final Map<String, ServiceHealth> serviceHealthCache = new ConcurrentHashMap<>();

    @Value("${gateway.health.check.interval:30}")
    private int healthCheckInterval;

    @Value("${gateway.health.timeout:5}")
    private int healthCheckTimeout;

    // 定时执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * 服务健康状态类
     */
    public static class ServiceHealth {
        private String serviceId;
        private boolean healthy;
        private int healthyInstances;
        private int totalInstances;
        private long lastCheckTime;
        private String lastMessage;
        private double responseTime;

        // 构造函数、getter和setter方法
        public ServiceHealth(String serviceId) {
            this.serviceId = serviceId;
            this.lastCheckTime = System.currentTimeMillis();
        }

        public String getServiceId() { return serviceId; }
        public boolean isHealthy() { return healthy; }
        public int getHealthyInstances() { return healthyInstances; }
        public int getTotalInstances() { return totalInstances; }
        public long getLastCheckTime() { return lastCheckTime; }
        public String getLastMessage() { return lastMessage; }
        public double getResponseTime() { return responseTime; }

        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public void setHealthyInstances(int healthyInstances) { this.healthyInstances = healthyInstances; }
        public void setTotalInstances(int totalInstances) { this.totalInstances = totalInstances; }
        public void setLastCheckTime(long lastCheckTime) { this.lastCheckTime = lastCheckTime; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
    }

    /**
     * 初始化健康检查服务
     */
    public void init() {
        // 确保参数为正数
        int interval = Math.max(1, healthCheckInterval);
        int timeout = Math.max(1, healthCheckTimeout);

        log.info("Initializing HealthCheckService with interval: {}s, timeout: {}s",
                interval, timeout);

        // 启动定时健康检查
        scheduler.scheduleAtFixedRate(
                this::performHealthChecks,
                10, // 初始延迟10秒
                interval,
                TimeUnit.SECONDS
        );

        // 启动缓存清理任务
        scheduler.scheduleAtFixedRate(
                this::cleanupExpiredCache,
                60, // 初始延迟60秒
                300, // 每5分钟清理一次
                TimeUnit.SECONDS
        );

        log.info("HealthCheckService initialized successfully");
    }

    /**
     * 执行健康检查
     */
    public void performHealthChecks() {
        try {
            List<String> services = discoveryClient.getServices();
            log.debug("Performing health checks for {} services", services.size());

            for (String serviceId : services) {
                checkServiceHealth(serviceId);
            }

            // 更新服务状态到缓存
            updateServiceStatusToCache();

        } catch (Exception e) {
            log.error("Error performing health checks", e);
        }
    }

    /**
     * 检查单个服务的健康状态
     */
    private void checkServiceHealth(String serviceId) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            ServiceHealth health = serviceHealthCache.computeIfAbsent(serviceId, ServiceHealth::new);

            if (instances.isEmpty()) {
                health.setHealthy(false);
                health.setHealthyInstances(0);
                health.setTotalInstances(0);
                health.setLastMessage("No instances available");
                health.setLastCheckTime(System.currentTimeMillis());
                log.warn("No instances found for service: {}", serviceId);
                return;
            }

            int healthyCount = 0;
            double totalResponseTime = 0;
            String lastMessage = "OK";

            for (ServiceInstance instance : instances) {
                try {
                    long startTime = System.currentTimeMillis();
                    boolean instanceHealthy = checkInstanceHealth(instance);
                    long responseTime = System.currentTimeMillis() - startTime;

                    totalResponseTime += responseTime;

                    if (instanceHealthy) {
                        healthyCount++;
                    } else {
                        lastMessage = "Instance " + instance.getInstanceId() + " is unhealthy";
                    }

                    // 缓存实例健康状态
                    cacheInstanceHealth(serviceId, instance.getInstanceId(), instanceHealthy, responseTime);

                } catch (Exception e) {
                    log.warn("Health check failed for instance {} of service {}: {}",
                            instance.getInstanceId(), serviceId, e.getMessage());
                }
            }

            // 更新服务健康状态
            health.setHealthy(healthyCount > 0);
            health.setHealthyInstances(healthyCount);
            health.setTotalInstances(instances.size());
            health.setLastMessage(lastMessage);
            health.setLastCheckTime(System.currentTimeMillis());
            health.setResponseTime(instances.size() > 0 ? totalResponseTime / instances.size() : 0);

            log.debug("Service health updated: {} - {}/{} instances healthy, response time: {}ms",
                    serviceId, healthyCount, instances.size(), health.getResponseTime());

        } catch (Exception e) {
            log.error("Error checking health for service: {}", serviceId, e);
            ServiceHealth health = serviceHealthCache.get(serviceId);
            if (health != null) {
                health.setHealthy(false);
                health.setLastMessage("Health check failed: " + e.getMessage());
                health.setLastCheckTime(System.currentTimeMillis());
            }
        }
    }

    /**
     * 检查单个实例的健康状态
     */
    private boolean checkInstanceHealth(ServiceInstance instance) {
        try {
            String healthUrl = instance.getUri().toString() + "/actuator/health";

            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.debug("Health check failed for instance {}: {}", instance.getInstanceId(), e.getMessage());
            return false;
        }
    }

    /**
     * 缓存实例健康状态
     */
    private void cacheInstanceHealth(String serviceId, String instanceId, boolean healthy, double responseTime) {
        String key = HEALTH_CACHE_PREFIX + serviceId + ":" + instanceId;

        Map<String, Object> healthData = new HashMap<>();
        healthData.put("serviceId", serviceId);
        healthData.put("instanceId", instanceId);
        healthData.put("healthy", healthy);
        healthData.put("responseTime", responseTime);
        healthData.put("checkTime", System.currentTimeMillis());

        redisTemplate.opsForValue().set(key, healthData, Duration.ofMinutes(5));
    }

    /**
     * 更新服务状态到缓存
     */
    private void updateServiceStatusToCache() {
        serviceHealthCache.forEach((serviceId, health) -> {
            String key = SERVICE_STATUS_PREFIX + serviceId;
            redisTemplate.opsForValue().set(key, health, Duration.ofMinutes(5));
        });
    }

    /**
     * 获取服务健康状态
     */
    public ServiceHealth getServiceHealth(String serviceId) {
        // 先从缓存获取
        ServiceHealth cached = serviceHealthCache.get(serviceId);
        if (cached != null) {
            return cached;
        }

        // 从Redis获取
        String key = SERVICE_STATUS_PREFIX + serviceId;
        Object cachedHealth = redisTemplate.opsForValue().get(key);
        if (cachedHealth instanceof ServiceHealth) {
            return (ServiceHealth) cachedHealth;
        }

        // 如果都没有，执行实时检查
        checkServiceHealth(serviceId);
        return serviceHealthCache.get(serviceId);
    }

    /**
     * 获取所有服务健康状态
     */
    public Map<String, ServiceHealth> getAllServicesHealth() {
        // 确保数据是最新的
        performHealthChecks();
        return new HashMap<>(serviceHealthCache);
    }

    /**
     * 检查服务是否健康
     */
    public boolean isServiceHealthy(String serviceId) {
        ServiceHealth health = getServiceHealth(serviceId);
        return health != null && health.isHealthy();
    }

    /**
     * 获取健康的实例
     */
    public List<ServiceInstance> getHealthyInstances(String serviceId) {
        if (!isServiceHealthy(serviceId)) {
            return Collections.emptyList();
        }

        List<ServiceInstance> allInstances = discoveryClient.getInstances(serviceId);
        List<ServiceInstance> healthyInstances = new ArrayList<>();

        for (ServiceInstance instance : allInstances) {
            String instanceKey = HEALTH_CACHE_PREFIX + serviceId + ":" + instance.getInstanceId();
            Object healthData = redisTemplate.opsForValue().get(instanceKey);

            if (healthData instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) healthData;
                Boolean healthy = (Boolean) data.get("healthy");
                if (Boolean.TRUE.equals(healthy)) {
                    healthyInstances.add(instance);
                }
            }
        }

        return healthyInstances;
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        try {
            // 清理过期的健康检查缓存
            Set<String> healthKeys = redisTemplate.keys(HEALTH_CACHE_PREFIX + "*");
            if (healthKeys != null && !healthKeys.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                for (String key : healthKeys) {
                    Object data = redisTemplate.opsForValue().get(key);
                    if (data instanceof Map) {
                        Map<String, Object> healthData = (Map<String, Object>) data;
                        Long checkTime = (Long) healthData.get("checkTime");
                        if (checkTime != null && currentTime - checkTime > 10 * 60 * 1000L) { // 10分钟
                            redisTemplate.delete(key);
                        }
                    }
                }
            }

            log.debug("Cleaned up expired health check cache");
        } catch (Exception e) {
            log.error("Error cleaning up expired cache", e);
        }
    }

    /**
     * 获取健康统计信息
     */
    public Map<String, Object> getHealthStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int totalServices = serviceHealthCache.size();
        int healthyServices = (int) serviceHealthCache.values().stream()
                .filter(ServiceHealth::isHealthy)
                .count();

        int totalInstances = serviceHealthCache.values().stream()
                .mapToInt(ServiceHealth::getTotalInstances)
                .sum();

        int healthyInstances = serviceHealthCache.values().stream()
                .mapToInt(ServiceHealth::getHealthyInstances)
                .sum();

        double avgResponseTime = serviceHealthCache.values().stream()
                .mapToDouble(ServiceHealth::getResponseTime)
                .average()
                .orElse(0.0);

        stats.put("totalServices", totalServices);
        stats.put("healthyServices", healthyServices);
        stats.put("unhealthyServices", totalServices - healthyServices);
        stats.put("totalInstances", totalInstances);
        stats.put("healthyInstances", healthyInstances);
        stats.put("unhealthyInstances", totalInstances - healthyInstances);
        stats.put("averageResponseTime", avgResponseTime);
        stats.put("healthPercentage", totalServices > 0 ? (healthyServices * 100.0 / totalServices) : 0.0);

        return stats;
    }

    /**
     * 销毁服务
     */
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("HealthCheckService destroyed");
    }
}