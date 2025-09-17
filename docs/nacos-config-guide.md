# Nacos配置中心使用指南

## 概述

本指南说明了如何为user-service配置和使用Nacos配置中心，实现配置的集中管理和动态刷新。

## 配置架构

### 配置优先级
1. **本地配置** (application.yml, bootstrap.yml)
2. **Nacos共享配置** (shared-configs)
3. **Nacos服务配置** (user-service.yaml)

### 配置文件分类
- **bootstrap.yml**: 本地基础配置，包含Nacos连接信息
- **application.yml**: 本地应用配置，包含无法远程化的配置
- **Nacos配置**: 中间件配置、业务配置、环境配置

## Nacos配置初始化

### 1. 创建命名空间
在Nacos控制台创建命名空间：
- **命名空间ID**: `test` (测试环境)
- **命名空间名称**: `测试环境`
- **描述**: 用户服务测试环境配置

### 2. 执行初始化脚本
```bash
# 连接到MySQL数据库
mysql -u root -p

# 执行Nacos配置初始化脚本
use nacos;
source /path/to/pp-cex/sql/02-init-nacos-config.sql;
```

### 3. 配置清单

#### 服务专用配置
- **Data ID**: `user-service.yaml`
- **Group**: `user-service`
- **内容**: 数据源、JPA、业务配置

#### 公共配置
- **Data ID**: `common-config.yaml`
- **Group**: `user-service`
- **内容**: 监控、日志、API文档配置

#### Redis配置
- **Data ID**: `redis-config.yaml`
- **Group**: `user-service`
- **内容**: Redis连接、缓存、Session配置

#### MySQL配置
- **Data ID**: `mysql-config.yaml`
- **Group**: `user-service`
- **内容**: 数据库连接、连接池、MyBatis配置

#### RocketMQ配置
- **Data ID**: `rocketmq-config.yaml`
- **Group**: `user-service`
- **内容**: 消息队列、消费者、生产者配置

## 配置详情

### user-service.yaml
```yaml
# 用户服务专用配置
spring:
  application:
    name: user-service

# 数据源配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:ppcex_user}?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true
    username: ${MYSQL_USER:cex_user}
    password: ${MYSQL_PASSWORD:cex123}
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
      pool-name: UserHikariCP

# 业务配置
cex:
  user:
    password-salt: ${PASSWORD_SALT:default-salt-value}
    default-password: ${DEFAULT_PASSWORD:User123456}
    register-reward: 0
    invite-reward: 0
    kyc-audit-hours: 24
    max-login-fail-times: 5
    login-lock-minutes: 30
    google-auth:
      enabled: ${GOOGLE_AUTH_ENABLED:true}
      issuer: ${GOOGLE_AUTH_ISSUE:CEX}
      window-size: 3
      code-digits: 6
    email-verification:
      enabled: ${EMAIL_VERIFICATION_ENABLED:true}
      expire-minutes: 30
    phone-verification:
      enabled: ${PHONE_VERIFICATION_ENABLED:true}
      expire-minutes: 10

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-jwt-secret-key-at-least-32-bytes-long-for-security}
  expiration: 86400000 # 24小时
  refresh-expiration: 604800000 # 7天

# 环境标识
env:
  name: test
  version: 1.0.0
```

### redis-config.yaml
```yaml
# Redis配置
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      username: ${REDIS_USER:cex_user}
      password: ${REDIS_PASSWORD:redis123}
      database: 0
      timeout: 10000ms
      lettuce:
        pool:
          max-active: 200
          max-wait: -1ms
          max-idle: 10
          min-idle: 0

# Redis缓存配置
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000 # 10分钟
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "user:"

# Redis session配置
spring:
  session:
    store-type: redis
    redis:
      namespace: user:session
    timeout: 1800 # 30分钟
```

### rocketmq-config.yaml
```yaml
# RocketMQ配置
rocketmq:
  name-server: ${ROCKETMQ_NAME_SERVER_ADDR:localhost:9876}
  producer:
    group: user-producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 3
    max-message-size: 4194304
    compress-message-body-threshold: 4096
  consumer:
    # 用户消息消费者
    user-consumer:
      group: user-consumer-group
      topic: user-topic
      selector-expression: "*"
      consume-thread-min: 20
      consume-thread-max: 64
      pull-batch-size: 32
    # 用户注册消费者
    register-consumer:
      group: register-consumer-group
      topic: register-topic
      selector-expression: "*"
      consume-thread-min: 10
      consume-thread-max: 20
      pull-batch-size: 16
    # 用户登录消费者
    login-consumer:
      group: login-consumer-group
      topic: login-topic
      selector-expression: "*"
      consume-thread-min: 10
      consume-thread-max: 20
      pull-batch-size: 16

# 消息轨迹配置
rocketmq:
  enable-msg-trace: true
  customized-trace-topic: rmq_sys_trace_topic
  access-key: ${ROCKETMQ_ACCESS_KEY:}
  secret-key: ${ROCKETMQ_SECRET_KEY:}
```

## 环境配置

### 测试环境配置
- **命名空间**: `test`
- **Group**: `user-service`
- **配置变量**:
  - `APP_ENV=test`
  - `NACOS_NAMESPACE=test`
  - `NACOS_GROUP=user-service`

### 生产环境配置
- **命名空间**: `prod`
- **Group**: `user-service`
- **配置变量**:
  - `APP_ENV=prod`
  - `NACOS_NAMESPACE=prod`
  - `NACOS_GROUP=user-service`

## 动态配置刷新

### 1. 开启配置刷新
在需要动态刷新的类上添加`@RefreshScope`注解：

```java
@RefreshScope
@RestController
public class UserController {
    @Value("${cex.user.max-login-fail-times}")
    private Integer maxLoginFailTimes;

    @GetMapping("/config")
    public String getConfig() {
        return "Max login fail times: " + maxLoginFailTimes;
    }
}
```

### 2. 配置监听器
创建配置监听器监听配置变更：

```java
@Component
public class UserConfigListener {

    @NacosConfigListener(dataId = "user-service.yaml", groupId = "user-service")
    public void onUserConfigChange(String newConfig) {
        log.info("User service config changed: {}", newConfig);
        // 处理配置变更逻辑
    }
}
```

## 配置最佳实践

### 1. 配置分类原则
- **本地配置**: 不常变化的配置（如服务端口、应用名称）
- **Nacos配置**: 需要动态调整的配置（如数据库连接、业务参数）
- **环境配置**: 不同环境差异化的配置（如数据库地址、日志级别）

### 2. 安全配置
- **敏感信息**: 使用环境变量传入，不在配置文件中明文存储
- **权限控制**: 设置Nacos的读写权限
- **配置加密**: 对敏感配置进行加密存储

### 3. 配置验证
- **启动验证**: 服务启动时验证配置的完整性
- **健康检查**: 通过Actuator检查配置状态
- **配置备份**: 定期备份重要配置

## 故障排查

### 1. 配置不生效
检查项：
- Nacos服务是否正常
- 网络连接是否正常
- 配置格式是否正确
- 命名空间和Group是否匹配

### 2. 配置刷新失败
检查项：
- 是否添加`@RefreshScope`注解
- 配置是否支持动态刷新
- 监听器是否正常工作

### 3. 常见问题
- **配置加载顺序**: 确保配置优先级正确
- **配置冲突**: 避免同名配置在不同文件中出现
- **配置丢失**: 检查Nacos配置历史记录

## 监控和维护

### 1. 配置监控
- 监控配置变更频率
- 监控配置加载时间
- 监控配置刷新成功率

### 2. 配置审计
- 记录配置变更历史
- 定期审查配置安全性
- 清理无用配置

### 3. 性能优化
- 合理设置配置刷新频率
- 避免频繁的配置变更
- 优化配置文件大小

## 参考文档

- [Nacos官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Alibaba文档](https://spring-cloud-alibaba.readthedocs.io/)
- [Spring Boot配置文档](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)