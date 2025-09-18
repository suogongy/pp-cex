# PPCEX Market Service

## 项目简介

PPCEX Market Service 是PPCEX交易所的行情服务模块，负责提供实时市场数据、K线图、深度图、成交明细等功能。

## 功能特性

### 核心功能
- **实时行情数据** - 24小时行情统计，包括最新价格、涨跌幅、成交量等
- **K线数据** - 支持1分钟到1月的多种时间间隔K线数据
- **深度数据** - 实时买卖盘深度数据
- **成交明细** - 最新成交记录和历史成交查询
- **交易对管理** - 交易对配置和管理

### 技术特性
- **高并发处理** - 支持大量WebSocket连接和实时数据推送
- **分布式架构** - 基于Spring Cloud Alibaba的微服务架构
- **消息队列** - 使用RocketMQ进行异步消息处理
- **缓存优化** - 多级缓存策略，保证高性能
- **实时推送** - WebSocket实时数据推送
- **数据聚合** - 自动数据统计和聚合

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.1.5 + Spring Cloud Alibaba 2022.0.4
- **服务注册**: Nacos 2.2.x
- **配置管理**: Nacos Config
- **消息队列**: RocketMQ 4.9.x
- **缓存**: Redis 7.x + Caffeine
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.4
- **文档**: Knife4j (Swagger)

### 架构模式
- **微服务**: Spring Cloud微服务架构
- **读写分离**: 数据库读写分离
- **消息驱动**: 基于RocketMQ的异步处理
- **缓存优先**: 多级缓存策略
- **实时通信**: WebSocket + STOMP

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7+
- RocketMQ 4.9+
- Nacos 2.2+

### 编译构建
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package -DskipTests
```

### 启动服务
```bash
# 启动基础服务 (MySQL, Redis, RocketMQ, Nacos)
docker-compose -f ../docker/docker-compose.yml up -d

# 启动应用
java -jar target/market-service-1.0.0.jar

# 或使用Maven启动
mvn spring-boot:run
```

### Docker部署
```bash
# 构建镜像
docker build -t ppcex/market-service:1.0.0 .

# 运行容器
docker run -d \
  --name market-service \
  -p 8005:8005 \
  -e SPRING_PROFILES_ACTIVE=prod \
  ppcex/market-service:1.0.0
```

## API文档

启动服务后，访问以下地址查看API文档：
- Swagger UI: http://localhost:8005/market/doc.html
- OpenAPI JSON: http://localhost:8005/market/v3/api-docs

### 主要API接口

#### 交易对管理
- `GET /market/api/v1/pairs` - 获取交易对列表
- `GET /market/api/v1/pairs/active` - 获取活跃交易对
- `GET /market/api/v1/pairs/symbol/{symbol}` - 根据符号获取交易对
- `POST /market/api/v1/pairs` - 添加交易对
- `PUT /market/api/v1/pairs/{id}` - 更新交易对
- `DELETE /market/api/v1/pairs/{id}` - 删除交易对

#### 行情数据
- `GET /market/api/v1/market/ticker/{symbol}` - 获取指定交易对行情
- `GET /market/api/v1/market/tickers` - 获取所有交易对行情
- `GET /market/api/v1/market/top/gainers` - 获取涨幅榜
- `GET /market/api/v1/market/top/losers` - 获取跌幅榜
- `GET /market/api/v1/market/top/volume` - 获取成交量榜

#### K线数据
- `GET /market/api/v1/market/klines` - 获取K线数据
  - 参数: symbol, interval, startTime, endTime, limit
  - 支持间隔: 1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w, 1M

#### 成交明细
- `GET /market/api/v1/market/trades` - 获取最新成交明细
  - 参数: symbol, limit

#### 深度数据
- `GET /market/api/v1/market/depth` - 获取深度数据
  - 参数: symbol, limit

### WebSocket连接

#### 连接地址
```
ws://localhost:8005/market/ws/market
```

#### 订阅行情
```json
{
  "id": "1",
  "method": "subscribe",
  "params": {
    "symbol": "BTCUSDT",
    "channel": "ticker"
  }
}
```

#### 订阅K线
```json
{
  "id": "2",
  "method": "subscribe",
  "params": {
    "symbol": "BTCUSDT",
    "channel": "kline_1m"
  }
}
```

#### 订阅成交
```json
{
  "id": "3",
  "method": "subscribe",
  "params": {
    "symbol": "BTCUSDT",
    "channel": "trade"
  }
}
```

#### 订阅深度
```json
{
  "id": "4",
  "method": "subscribe",
  "params": {
    "symbol": "BTCUSDT",
    "channel": "depth"
  }
}
```

## 配置说明

### Nacos配置
服务使用Nacos进行配置管理，需要创建以下配置：

#### 1. 基础配置 (common-config.yaml)
- 服务基本信息
- 线程池配置
- 缓存配置
- 安全配置

#### 2. 数据库配置 (mysql-config.yaml)
- 数据源配置
- MyBatis Plus配置
- 数据迁移配置

#### 3. Redis配置 (redis-config.yaml)
- Redis连接配置
- 缓存策略配置
- 发布订阅配置

#### 4. 消息队列配置 (rocketmq-config.yaml)
- RocketMQ连接配置
- 消息主题配置
- 生产者消费者配置

### 环境配置
支持多环境配置：
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境

## 监控运维

### 健康检查
```bash
# 服务健康状态
curl http://localhost:8005/market/actuator/health

# 服务信息
curl http://localhost:8005/market/actuator/info

# 指标数据
curl http://localhost:8005/market/actuator/metrics
```

### 日志配置
- 日志文件: `logs/market-service.log`
- 日志级别: INFO (可配置)
- 日志格式: 包含traceId的格式化日志

### 性能监控
- JVM监控
- 数据库连接池监控
- 缓存命中率监控
- 消息队列监控

## 数据模型

### 核心表结构
- `market_pair` - 交易对配置表
- `market_ticker` - 行情数据表
- `market_kline` - K线数据表
- `market_trade` - 成交明细表
- `market_depth` - 深度数据表
- `market_statistics` - 市场统计表

### 数据关系
```
market_pair (1) -> (N) market_ticker
market_pair (1) -> (N) market_kline
market_pair (1) -> (N) market_trade
market_pair (1) -> (N) market_depth
```

## 开发指南

### 代码结构
```
src/main/java/com/ppcex/market/
├── controller/          # 控制器层
├── service/            # 服务层
├── service/impl/       # 服务实现层
├── mapper/            # 数据访问层
├── entity/            # 实体类
├── dto/               # 数据传输对象
├── config/            # 配置类
├── mq/                # 消息队列
├── websocket/         # WebSocket处理器
└── enums/            # 枚举类
```

### 开发规范
- 遵循阿里巴巴Java开发手册
- 使用统一的异常处理
- 添加详细的API文档注解
- 编写单元测试
- 使用缓存注解优化性能

### 测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=MarketPairServiceTest

# 生成测试报告
mvn surefire-report:report
```

## 部署说明

### 单机部署
```bash
# 1. 启动依赖服务
docker-compose -f ../docker/docker-compose.yml up -d

# 2. 初始化数据库
mysql -h localhost -u root -p market_db < src/main/resources/db/migration/V001__Create_Market_Tables.sql

# 3. 启动应用
java -jar target/market-service-1.0.0.jar
```

### 集群部署
```bash
# 1. 配置Nacos集群
# 2. 配置Redis集群
# 3. 配置RocketMQ集群
# 4. 配置MySQL主从
# 5. 启动多个实例
java -jar target/market-service-1.0.0.jar --server.port=8005
java -jar target/market-service-1.0.0.jar --server.port=8006
java -jar target/market-service-1.0.0.jar --server.port=8007
```

### 负载均衡
推荐使用Nginx进行负载均衡：
```nginx
upstream market-service {
    server localhost:8005;
    server localhost:8006;
    server localhost:8007;
}

server {
    listen 80;
    server_name market.ppcex.com;

    location / {
        proxy_pass http://market-service;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 性能优化

### 缓存策略
- **本地缓存**: Caffeine缓存热点数据
- **Redis缓存**: 分布式缓存，避免缓存击穿
- **多级缓存**: L1 + L2缓存策略

### 数据库优化
- **读写分离**: 主库写入，从库读取
- **索引优化**: 合理的索引设计
- **分库分表**: 大数据量时进行分片

### 消息队列优化
- **批量处理**: 减少消息数量
- **异步处理**: 提高响应速度
- **顺序消费**: 保证数据一致性

## 故障排除

### 常见问题
1. **服务启动失败**: 检查依赖服务是否正常
2. **数据库连接失败**: 检查数据库配置和连接
3. **缓存失效**: 检查Redis连接和配置
4. **消息堆积**: 检查RocketMQ状态和消费者

### 日志分析
```bash
# 查看错误日志
grep ERROR logs/market-service.log

# 查看特定服务日志
grep "MarketPairService" logs/market-service.log

# 查看性能日志
grep "performance" logs/market-service.log
```

### 监控告警
- 服务可用性监控
- 数据库连接监控
- 缓存命中率监控
- 消息队列延迟监控

## 版本历史

### v1.0.0 (2024-09-18)
- 初始版本发布
- 实现基础行情数据功能
- 支持WebSocket实时推送
- 集成消息队列和缓存

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request
5. 代码审查

## 许可证

MIT License

## 联系方式

- 项目地址: https://github.com/ppcex/market-service
- 问题反馈: https://github.com/ppcex/market-service/issues
- 邮箱: dev@ppcex.com