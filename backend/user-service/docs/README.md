# User Service 配置文件说明

## 概述

这些配置文件用于配置user-service微服务的各个组件，通过Nacos配置中心进行统一管理和动态更新。

## 配置文件列表

### 1. common-config.yaml
**用途**: 通用配置，包含所有环境的基础配置
- 服务基础信息
- 业务配置（JWT、密码、登录、KYC等）
- 安全配置（跨域、限流等）
- 监控配置（Actuator、Metrics等）
- 日志配置
- 线程池配置
- 缓存配置

### 2. redis-config.yaml
**用途**: Redis缓存配置
- Spring Data Redis配置
- Redisson分布式锁配置
- 缓存策略配置
- Redis集群/Sentinel配置
- Lua脚本配置
- 性能监控配置

### 3. mysql-config.yaml
**用途**: MySQL数据库配置
- 数据源配置（HikariCP连接池）
- MyBatis Plus配置
- Flyway数据库版本控制
- 读写分离配置
- 分库分表配置
- 数据库监控配置

### 4. rocketmq-config.yaml
**用途**: RocketMQ消息队列配置
- 生产者/消费者配置
- 消息主题配置
- 事务消息配置
- 消息追踪配置
- 延迟消息配置
- 幂等性配置
- 死信队列配置

## Nacos配置中心使用

### 配置导入
1. 登录Nacos控制台
2. 选择对应的命名空间（public/dev/test/prod）
3. 在配置管理中创建配置
4. 将各配置文件内容复制到对应的配置项中

### 配置优先级
```
bootstrap.yml > shared-configs > 本地application.yml
```

### 环境配置策略

#### 开发环境
- 使用默认值，便于本地开发
- 日志级别设置为DEBUG
- 数据库连接使用本地数据库

#### 测试环境
- 通过环境变量覆盖关键配置
- 日志级别设置为INFO
- 连接测试环境的中间件

#### 生产环境
- 所有敏感配置通过环境变量注入
- 日志级别设置为WARN
- 启用集群配置和监控

## 配置更新策略

### 动态刷新
支持动态刷新的配置：
- Redis连接配置
- 日志级别配置
- 业务开关配置
- 限流规则配置

### 重启生效
需要重启生效的配置：
- 数据库连接配置
- RocketMQ连接配置
- 线程池配置
- 监控配置

## 安全注意事项

1. **敏感信息保护**
   - 密码、密钥等信息使用环境变量
   - 不要将真实密码提交到版本控制

2. **配置隔离**
   - 不同环境使用不同的命名空间
   - 生产环境配置需要特殊权限管理

3. **配置备份**
   - 定期备份重要配置
   - 建立配置变更审计机制

## 配置验证

### 健康检查
```bash
# 检查服务状态
curl http://localhost:8001/user/actuator/health

# 检查配置信息
curl http://localhost:8001/user/actuator/configprops
```

### 连接测试
```bash
# 测试数据库连接
curl http://localhost:8001/user/actuator/health/db

# 测试Redis连接
curl http://localhost:8001/user/actuator/health/redis

# 测试RocketMQ连接
curl http://localhost:8001/user/actuator/health/rocketmq
```

## 常见问题

### 1. 配置不生效
- 检查Nacos连接配置
- 确认配置文件格式正确
- 查看服务启动日志

### 2. 连接池异常
- 调整连接池参数
- 检查网络连接
- 监控连接池使用情况

### 3. 消息消费异常
- 检查消费者组配置
- 确认主题权限设置
- 查看消息消费日志

## 配置示例

### Docker环境变量配置
```bash
# docker-compose.yml
environment:
  - NACOS_SERVER_ADDR=nacos-server:8848
  - REDIS_HOST=redis-server
  - MYSQL_HOST=mysql-server
  - ROCKETMQ_NAME_SERVER_ADDR=rocketmq-nameserver:9876
  - JWT_SECRET=your-secret-key
  - DB_PASSWORD=your-db-password
```

### Kubernetes ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-service-config
data:
  LOG_LEVEL: "INFO"
  SPRING_PROFILES_ACTIVE: "prod"
  REDIS_HOST: "redis-service"
  MYSQL_HOST: "mysql-service"
```

## 监控和告警

### 关键指标
- 配置加载成功率
- 连接池使用率
- 消息发送/消费成功率
- 缓存命中率

### 告警规则
- 配置加载失败
- 连接池满
- 消息积压
- 缓存命中率下降

## 维护建议

1. **定期检查配置文件**
2. **监控配置变更**
3. **备份重要配置**
4. **测试配置更新**
5. **文档化配置变更**

---

## 联系方式

如有配置相关问题，请联系：
- 开发团队：dev-team@example.com
- 运维团队：ops-team@example.com