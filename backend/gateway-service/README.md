# PPCEX Gateway Service

## 项目概述

Gateway Service是PPCEX系统的统一API网关，作为所有外部请求的统一入口，提供路由转发、负载均衡、认证授权、限流熔断、监控日志等核心功能。

## 技术栈

- **框架**: Spring Boot 3.x + Spring Cloud Gateway
- **服务发现**: Nacos
- **配置管理**: Nacos Config
- **限流熔断**: Sentinel + Resilience4j
- **缓存**: Redis + Caffeine
- **监控**: Prometheus + Actuator
- **文档**: Knife4j
- **构建工具**: Maven

## 核心功能

### 1. 路由管理
- 动态路由配置
- 服务发现路由
- 路由负载均衡
- 路由健康检查

### 2. 认证授权
- JWT令牌验证
- RBAC权限控制
- IP黑白名单
- 请求签名验证

### 3. 流量控制
- IP限流
- 用户限流
- API限流
- Sentinel流控

### 4. 安全防护
- XSS防护
- SQL注入防护
- CSRF防护
- 恶意请求检测

### 5. 熔断降级
- 服务熔断
- 降级策略
- 自动恢复
- 故障转移

### 6. 监控日志
- 访问日志记录
- 性能指标监控
- 链路追踪
- 告警通知

## 项目结构

```
gateway-service/
├── src/main/java/com/ppcex/gateway/
│   ├── GatewayServiceApplication.java    # 启动类
│   ├── config/                           # 配置类
│   │   ├── GatewayConfig.java            # 网关配置
│   │   ├── SentinelConfig.java          # Sentinel配置
│   │   └── RedisConfig.java             # Redis配置
│   ├── filter/                          # 过滤器
│   │   ├── AuthenticationFilter.java     # 认证过滤器
│   │   ├── RateLimitFilter.java         # 限流过滤器
│   │   ├── SecurityFilter.java          # 安全过滤器
│   │   └── AccessLogFilter.java         # 访问日志过滤器
│   ├── service/                         # 服务类
│   │   └── DynamicRouteService.java     # 动态路由服务
│   ├── model/                           # 数据模型
│   │   └── AccessLog.java              # 访问日志模型
│   └── controller/                      # 控制器
│       └── GatewayController.java       # 网关管理接口
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   └── docs/                           # 配置文档
│       ├── gateway-routes.yaml         # 路由配置
│       ├── gateway-security.yaml       # 安全配置
│       ├── gateway-rate-limit.yaml     # 限流配置
│       ├── gateway-circuit-breaker.yaml # 熔断配置
│       ├── gateway-cache.yaml          # 缓存配置
│       └── common-config.yaml          # 通用配置
├── docs/
│   └── gateway-service-design.md       # 技术设计文档
├── pom.xml                             # Maven配置
└── README.md                           # 项目说明
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Redis 7+
- Nacos 2.2+
- Sentinel 1.8+

### 1. 构建项目

```bash
# 编译项目
mvn clean compile

# 打包项目
mvn clean package

# 运行测试
mvn test
```

### 2. 启动服务

```bash
# 启动服务
java -jar target/gateway-service-1.0.0.jar

# 或者使用Maven启动
mvn spring-boot:run
```

### 3. 验证服务

```bash
# 检查健康状态
curl http://localhost:9000/actuator/health

# 查看服务信息
curl http://localhost:9000/actuator/info

# 查看路由信息
curl http://localhost:9000/actuator/gateway/routes
```

## 配置说明

### Nacos配置

网关服务使用Nacos作为配置中心，配置文件结构如下：

```
gateway-service/
├── gateway-routes.yaml          # 路由配置
├── gateway-security.yaml        # 安全配置
├── gateway-rate-limit.yaml      # 限流配置
├── gateway-circuit-breaker.yaml  # 熔断配置
├── gateway-cache.yaml           # 缓存配置
└── common-config.yaml           # 通用配置
```

### 环境配置

支持多环境配置，通过`spring.profiles.active`指定：

- `dev`: 开发环境
- `test`: 测试环境
- `prod`: 生产环境

### 关键配置项

```yaml
# 服务端口
server:
  port: 9000

# Nacos配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: d8a0e588-e615-448b-994c-0ad931c56808
        group: gateway-service

# Redis配置
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 1

# Sentinel配置
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8858
```

## API文档

### 管理接口

- `GET /api/v1/gateway/routes` - 获取所有路由
- `POST /api/v1/gateway/routes/refresh` - 刷新路由
- `GET /api/v1/gateway/logs` - 查询访问日志
- `GET /api/v1/gateway/statistics` - 获取访问统计
- `GET /api/v1/gateway/health` - 获取健康状态

### 监控接口

- `GET /actuator/health` - 健康检查
- `GET /actuator/info` - 服务信息
- `GET /actuator/metrics` - 性能指标
- `GET /actuator/prometheus` - Prometheus指标
- `GET /actuator/gateway/routes` - 网关路由

## 核心组件说明

### 1. 过滤器

#### AuthenticationFilter
- JWT令牌验证
- 用户状态检查
- 令牌黑名单检查
- 用户信息添加

#### RateLimitFilter
- IP限流
- 用户限流
- API限流
- Lua脚本限流

#### SecurityFilter
- IP黑白名单检查
- SQL注入防护
- XSS攻击防护
- User-Agent检查

#### AccessLogFilter
- 访问日志记录
- 性能指标收集
- 统计信息生成
- 异步日志处理

### 2. 配置类

#### GatewayConfig
- 限流解析器配置
- KeyResolver定义
- 路由策略配置

#### SentinelConfig
- Sentinel规则配置
- 限流处理器
- 熔断规则配置

#### RedisConfig
- Redis连接配置
- 序列化器配置
- 连接池配置

### 3. 服务类

#### DynamicRouteService
- 动态路由管理
- Nacos配置监听
- 路由更新处理

## 部署说明

### 1. Docker部署

```bash
# 构建镜像
docker build -t ppcex/gateway-service:1.0.0 .

# 运行容器
docker run -d \
  --name gateway-service \
  -p 9000:9000 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e NACOS_SERVER_ADDR=nacos-server:8848 \
  -e REDIS_HOST=redis-server \
  ppcex/gateway-service:1.0.0
```

### 2. Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: gateway-service
  template:
    metadata:
      labels:
        app: gateway-service
    spec:
      containers:
      - name: gateway-service
        image: ppcex/gateway-service:1.0.0
        ports:
        - containerPort: 9000
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: NACOS_SERVER_ADDR
          value: "nacos-server:8848"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 9000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 9000
          initialDelaySeconds: 5
          periodSeconds: 5
```

## 监控与告警

### 1. 性能指标

- **请求量**: 总请求数、成功请求数、失败请求数
- **响应时间**: 平均响应时间、P95响应时间、P99响应时间
- **错误率**: HTTP错误率、业务错误率
- **并发数**: 当前并发连接数、最大并发连接数

### 2. 业务指标

- **路由状态**: 路由健康状态、路由负载
- **限流状态**: 限流触发次数、限流恢复时间
- **熔断状态**: 熔断器状态、熔断恢复时间
- **缓存状态**: 缓存命中率、缓存大小

### 3. 告警规则

- 高错误率告警
- 响应时间告警
- 熔断器打开告警
- 缓存命中率低告警

## 故障排查

### 1. 常见问题

#### 服务启动失败
- 检查Nacos连接是否正常
- 检查Redis连接是否正常
- 检查端口是否被占用

#### 路由不生效
- 检查Nacos配置是否正确
- 检查服务发现是否正常
- 查看路由日志

#### 限流触发
- 检查限流配置是否合理
- 查看限流统计信息
- 调整限流阈值

### 2. 日志分析

```bash
# 查看应用日志
tail -f logs/gateway-service.log

# 查看错误日志
grep ERROR logs/gateway-service.log

# 查看访问日志
grep "Access log" logs/gateway-service.log
```

### 3. 监控指标

```bash
# 查看健康状态
curl http://localhost:9000/actuator/health

# 查看性能指标
curl http://localhost:9000/actuator/metrics

# 查看Prometheus指标
curl http://localhost:9000/actuator/prometheus
```

## 开发指南

### 1. 添加新过滤器

```java
@Component
public class CustomFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 过滤器逻辑
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0; // 过滤器顺序
    }
}
```

### 2. 添加新路由

```yaml
# 在gateway-routes.yaml中添加
routes:
  - id: new-service
    uri: lb://new-service
    predicates:
      - Path=/api/v1/new/**
    filters:
      - StripPrefix=2
    metadata:
      service-name: new-service
      version: 1.0.0
```

### 3. 配置限流规则

```yaml
# 在gateway-rate-limit.yaml中添加
api-limits:
  - path: "/api/v1/new/**"
    window-seconds: 60
    max-requests: 100
    burst-capacity: 200
    algorithm: sliding-window
```

## 性能优化

### 1. 缓存优化
- 启用本地缓存
- 优化Redis连接池
- 合理设置缓存过期时间

### 2. 线程池优化
- 调整事件循环线程数
- 优化业务线程池
- 配置合理的队列大小

### 3. 连接优化
- 优化HTTP连接池
- 配置合理的超时时间
- 启用连接复用

## 版本更新

### v1.0.0 (2024-01-01)
- 初始版本发布
- 支持基本的路由、认证、限流功能
- 集成Nacos、Redis、Sentinel

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 许可证

MIT License

## 联系方式

- 项目地址: https://github.com/ppcex/gateway-service
- 问题反馈: https://github.com/ppcex/gateway-service/issues
- 技术支持: support@ppcex.com

## 相关文档

- [技术设计文档](docs/gateway-service-design.md)
- [API文档](http://localhost:9000/doc.html)
- [Nacos配置指南](../docs/nacos-config-guide.md)