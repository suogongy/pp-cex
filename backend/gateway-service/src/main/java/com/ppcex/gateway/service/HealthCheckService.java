package com.ppcex.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HealthCheckService {

    private final DiscoveryClient discoveryClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    // 初始化状态标志，防止重复初始化
    private volatile boolean initialized = false;

    public HealthCheckService(DiscoveryClient discoveryClient, RedisTemplate<String, Object> redisTemplate, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
    }

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
    @PostConstruct
    public void init() {
        if (initialized) {
            log.warn("HealthCheckService already initialized, skipping duplicate initialization");
            return;
        }

        // 确保参数为正数
        int interval = Math.max(30, healthCheckInterval); // 最小30秒
        int timeout = Math.max(5, healthCheckTimeout);    // 最小5秒

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

        initialized = true;
        log.info("HealthCheckService initialized successfully with interval: {}s", interval);
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

        if (instance == null) {
            log.warn("实例为null，无法进行健康检查");
            return false;
        }
        String instanceId = generateInstanceId(instance);
        log.debug("开始检查实例健康状态 - 实例ID: {}",  generateInstanceId(instance)); 

        String instanceUri = instance.getUri() != null ? instance.getUri().toString() : "null";

        log.debug("实例基本信息 - 实例ID: {} URI: {}", instanceId, instanceUri);

        try {
            // 检查URI是否有效
            if (instance.getUri() == null) {
                log.warn("实例URI为null - 实例ID: {}", instanceId);
                return false;
            }

            // 获取服务的context-path
            String contextPath = getContextPath(instance);
            log.debug("获取到context-path: {}", contextPath);

            // 构建健康检查URL
            String healthUrl = instanceUri + contextPath + "/actuator/health";
            log.debug("构建健康检查URL - URL: {}", healthUrl);

            // 测试多个可能的健康检查URL
            String[] possibleUrls = {
                healthUrl,                                    // 带context-path的标准路径
                instanceUri + "/actuator/health",             // 不带context-path
                instanceUri + contextPath + "/actuator/health" // 双重context-path（容错）
            };

            for (int i = 0; i < possibleUrls.length; i++) {
                String testUrl = possibleUrls[i];
                log.debug("尝试第{}个URL: {}", i + 1, testUrl);

                try {
                    long startTime = System.currentTimeMillis();
                    ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);
                    long responseTime = System.currentTimeMillis() - startTime;

                    org.springframework.http.HttpStatusCode statusCode = response.getStatusCode();
                    String responseBody = response.getBody();

                    // 检查HTTP状态码和JSON响应中的状态
                    boolean isHttpHealthy = statusCode.is2xxSuccessful();
                    boolean isServiceHealthy = checkServiceHealthStatus(responseBody);
                    boolean isOverallHealthy = isHttpHealthy && isServiceHealthy;

                    log.debug("URL检查结果 - URL: {} HTTP状态码: {} 服务状态: {} 响应时间: {}ms 整体健康: {}",
                            testUrl, statusCode, isServiceHealthy ? "UP" : "DOWN", responseTime,
                            isOverallHealthy ? "健康" : "不健康");

                    if (isOverallHealthy) {
                        log.debug("实例健康检查成功 - 实例ID: {} 使用的URL: {} 响应时间: {}ms",
                                instanceId, testUrl, responseTime);
                        return true;
                    } else if (isHttpHealthy) {
                        // HTTP成功但服务状态为DOWN，记录详细信息
                        log.warn("实例HTTP可访问但服务状态为DOWN - 实例ID: {} URL: {} 响应: {}",
                                instanceId, testUrl, responseBody != null ? responseBody.substring(0, Math.min(responseBody.length(), 200)) : "null");
                    }

                } catch (Exception e) {
                    log.debug("URL检查失败 - URL: {} 错误: {}", testUrl, e.getMessage());
                    if (i == possibleUrls.length - 1) {
                        // 最后一个URL也失败了
                        log.warn("所有健康检查URL都失败 - 实例ID: {} 最后尝试的URL: {}", instanceId, testUrl);
                    }
                }
            }

            log.warn("实例健康检查失败 - 实例ID: {} 尝试了{}个URL都失败", instanceId, possibleUrls.length);
            return false;

        } catch (Exception e) {
            log.error("实例健康检查异常 - 实例ID: {} URI: {} 错误: {}",
                    instanceId, instanceUri, e.getMessage());
            return false;
        }
    }

    private String generateInstanceId(ServiceInstance instance) {
        if (instance.getInstanceId() != null && !instance.getInstanceId().trim().isEmpty()) {
            return instance.getInstanceId();
        }
        // 使用IP:端口作为实例ID
        return instance.getHost() + ":" + instance.getPort();
    }

    /**
     * 检查服务健康状态（从JSON响应中解析）
     */
    private boolean checkServiceHealthStatus(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.debug("响应体为空，认为服务不健康");
            return false;
        }

        try {
            // 简单的字符串匹配，查找 "status":"UP" 或 "status":"DOWN"
            if (responseBody.contains("\"status\":\"UP\"")) {
                log.debug("从响应体中检测到服务状态: UP");
                return true;
            } else if (responseBody.contains("\"status\":\"DOWN\"")) {
                log.debug("从响应体中检测到服务状态: DOWN");
                return false;
            } else if (responseBody.contains("\"status\":\"UNKNOWN\"")) {
                log.debug("从响应体中检测到服务状态: UNKNOWN");
                return false;
            } else {
                // 如果没有明确的状态，检查是否有关键组件的健康状态
                if (responseBody.contains("\"discoveryComposite\":{\"status\":\"UP\"")) {
                    log.debug("发现发现服务组件健康，认为服务整体健康");
                    return true;
                }
                log.debug("无法从响应体中确定服务状态，默认认为不健康");
                return false;
            }
        } catch (Exception e) {
            log.debug("解析服务健康状态时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取服务的context-path
     */
    private String getContextPath(ServiceInstance instance) {
        // 从元数据中获取context-path
        Map<String, String> metadata = instance.getMetadata();
        if (metadata != null) {
            String contextPath = metadata.get("context-path");
            if (contextPath != null && !contextPath.trim().isEmpty()) {
                log.debug("从元数据获取context-path: {}", contextPath);
                return contextPath;
            }
        }

        // 默认为空字符串
        log.debug("使用默认context-path: 空");
        return "";
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
    @PreDestroy
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