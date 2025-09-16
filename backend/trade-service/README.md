# CEX Trade Service

## 服务概述

交易服务是CEX系统的核心组件，负责处理用户订单、订单撮合、成交记录等核心交易功能。

## 主要功能

### 1. 交易对管理
- 交易对配置和维护
- 支持多种交易对（BTC/USDT, ETH/USDT等）
- 交易对状态管理（启用/暂停）
- 手续费率配置

### 2. 订单管理
- 限价单和市价单
- 买入和卖出订单
- 订单状态管理（待成交、部分成交、完全成交、已取消）
- 订单生命周期管理
- 订单取消和超时处理

### 3. 撮合引擎
- 高性能内存撮合
- 价格时间优先算法
- 实时订单簿管理
- 支持Maker/Taker费率模式

### 4. 成交记录
- 详细的成交历史
- 实时成交数据
- 用户成交记录查询
- 市场成交数据统计

### 5. 消息队列集成
- RocketMQ事务消息
- 订单创建、成交、取消消息
- 资产变动消息
- 异步处理和最终一致性

## 技术架构

### 核心技术栈
- **Spring Boot 3.2.0** - 微服务框架
- **MyBatis Plus** - ORM框架
- **Redis** - 缓存和分布式锁
- **MySQL** - 数据持久化
- **RocketMQ** - 消息队列
- **Nacos** - 服务注册发现

### 关键设计模式
- **内存订单簿** - 高性能撮合引擎
- **分布式锁** - 防止重复下单
- **事务消息** - 保证数据一致性
- **异步处理** - 提升系统性能

## API接口

### 交易对管理
```
GET /api/trade/pairs - 获取交易对列表
GET /api/trade/pairs/active - 获取活跃交易对
GET /api/trade/pairs/{symbol} - 根据符号获取交易对
POST /api/trade/pairs - 添加交易对
PUT /api/trade/pairs/{id} - 更新交易对
DELETE /api/trade/pairs/{id} - 删除交易对
```

### 订单管理
```
POST /api/trade/orders - 创建订单
POST /api/trade/orders/cancel - 取消订单
GET /api/trade/orders/{orderNo} - 根据订单号获取订单
GET /api/trade/orders - 获取用户订单列表
GET /api/trade/orders/active - 获取用户当前委托
```

### 交易接口
```
GET /api/trade/trades - 获取成交记录
GET /api/trade/trades/recent - 获取最近成交记录
GET /api/trade/trades/user - 获取用户成交记录
GET /api/trade/orderbook/{symbol} - 获取订单簿
GET /api/trade/price/{symbol} - 获取最新成交价格
```

## 配置说明

### 应用配置
- `server.port=8002` - 服务端口
- `spring.datasource.*` - 数据库配置
- `spring.redis.*` - Redis配置
- `spring.rocketmq.*` - RocketMQ配置

### 系统参数
- 订单频率限制：每分钟10单
- 撮合引擎：内存优先级队列
- 订单簿缓存：1分钟过期
- 价格数据缓存：5分钟过期

## 部署说明

### 本地运行
```bash
# 1. 编译项目
mvn clean package

# 2. 启动服务
java -jar target/trade-service-1.0.0.jar
```

### Docker运行
```bash
# 构建镜像
docker build -t trade-service:1.0.0 .

# 运行容器
docker run -d -p 8002:8002 --name trade-service trade-service:1.0.0
```

### Docker Compose
```bash
# 启动所有服务
docker-compose -f docker-compose.dev.yml up -d

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f trade-service
```

## 监控指标

### 业务指标
- 订单创建量
- 订单成交率
- 撮合延迟
- 消息处理量

### 系统指标
- 内存使用率
- CPU使用率
- 数据库连接数
- Redis缓存命中率

## 测试用例

### 订单创建测试
```bash
curl -X POST "http://localhost:8002/api/trade/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTCUSDT",
    "orderType": 1,
    "direction": 1,
    "price": "50000.00",
    "amount": "0.001"
  }'
```

### 订单查询测试
```bash
curl "http://localhost:8002/api/trade/orders?page=1&size=10"
```

### 订单簿查询测试
```bash
curl "http://localhost:8002/api/trade/orderbook/BTCUSDT"
```

## 注意事项

1. **数据一致性**：使用RocketMQ事务消息确保订单和资产变动的一致性
2. **性能优化**：内存撮合引擎支持高并发订单处理
3. **安全控制**：实现订单频率限制和分布式锁
4. **监控告警**：配置关键业务指标监控
5. **扩展性**：支持水平扩展和负载均衡

## 故障排查

### 常见问题
1. **订单创建失败**：检查用户余额和交易对状态
2. **撮合延迟**：检查系统负载和内存使用情况
3. **消息积压**：检查RocketMQ服务状态
4. **数据库连接**：检查MySQL连接池配置

### 日志分析
```bash
# 查看应用日志
tail -f logs/trade-service.log

# 查看错误日志
grep ERROR logs/trade-service.log
```

## 版本历史

- **v1.0.0** - 初始版本，支持基础交易功能
  - 交易对管理
  - 订单管理
  - 撮合引擎
  - 成交记录
  - RocketMQ集成