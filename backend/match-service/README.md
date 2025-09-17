# Match Service - 高性能撮合引擎服务

## 服务概述

Match Service 是一个基于 Spring Boot 的高性能撮合引擎服务，采用 Disruptor 框架实现毫秒级订单处理能力，支持多交易对并发撮合。

## 核心特性

### 🚀 高性能撮合引擎
- **Disruptor 框架**: 基于 LMAX Disruptor 实现无锁化订单处理
- **内存订单簿**: 使用 ConcurrentSkipListMap 实现高效的订单管理
- **并发处理**: 支持多交易对并行撮合
- **低延迟**: 订单处理延迟 < 1ms

### 💼 完整的撮合功能
- **限价单撮合**: 支持价格时间优先算法
- **市价单撮合**: 支持最优价格成交
- **部分成交**: 支持订单部分成交处理
- **订单生命周期**: 完整的订单状态管理

### 🔗 消息队列集成
- **RocketMQ**: 异步订单处理，确保高可用
- **事务消息**: 保证订单创建和撮合的一致性
- **顺序消息**: 确保订单处理顺序
- **重试机制**: 自动重试失败订单

### 📊 实时数据推送
- **WebSocket**: 实时订单簿和成交数据推送
- **Redis缓存**: 高速数据缓存和共享
- **订阅机制**: 支持多客户端实时订阅
- **广播机制**: 全局市场数据广播

### 🛡️ 高可用设计
- **服务注册**: 集成 Nacos 服务发现
- **配置管理**: Nacos 配置中心
- **熔断限流**: Sentinel 流量控制
- **健康检查**: 完整的监控指标

## 技术架构

### 核心组件

```
┌─────────────────────────────────────────────────────────────┐
│                      应用层                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ REST API    │  │ WebSocket   │  │ 消息队列     │          │
│  │ Controller  │  │ Controller  │  │ Consumer    │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      业务层                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ OrderService│  │ TradeService│  │ Matching    │          │
│  │             │  │             │  │ Engine      │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      引擎层                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ OrderBook   │  │ Disruptor   │  │ Event       │          │
│  │ Manager     │  │ Processor   │  │ Handler     │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      基础设施层                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ Redis       │  │ RocketMQ    │  │ Nacos       │          │
│  │ 缓存        │  │ 消息队列     │  │ 服务发现     │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### 撮合算法

#### 价格时间优先算法
1. **买方优先级**: 价格从高到低，时间从早到晚
2. **卖方优先级**: 价格从低到高，时间从早到晚
3. **撮合条件**: 买方价格 >= 卖方价格
4. **成交价格**: 以先到订单的价格为准

#### 订单簿管理
- **买盘(Bids)**: 按价格降序排列
- **卖盘(Asks)**: 按价格升序排列
- **深度聚合**: 支持多档深度显示
- **实时更新**: 订单变更实时推送

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Redis 6.0+
- RocketMQ 4.9+
- Nacos 2.2+
- MySQL 8.0+

### 启动服务

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动服务
mvn spring-boot:run

# 或使用Docker
docker-compose up -d match-service
```

### 服务端口
- **REST API**: 8005
- **WebSocket**: 8005/ws/match
- **健康检查**: 8005/match/actuator/health

## API 接口

### REST API

#### 订单簿查询
```http
POST /match/orderbook
Content-Type: application/json

{
  "symbol": "BTCUSDT",
  "depth": 20,
  "precision": 8
}
```

#### 最新价格查询
```http
GET /match/price/BTCUSDT
```

#### 交易历史查询
```http
POST /match/trade/history
Content-Type: application/json

{
  "symbol": "BTCUSDT",
  "limit": 100
}
```

#### 活跃交易对
```http
GET /match/symbols
```

### WebSocket 订阅

#### 订单簿订阅
```javascript
const socket = new WebSocket('ws://localhost:8005/match/ws/match');
socket.send(JSON.stringify({
  destination: '/app/subscribe/BTCUSDT'
}));
socket.subscribe('/topic/orderbook/BTCUSDT', (message) => {
  console.log('订单簿更新:', JSON.parse(message.body));
});
```

#### 价格订阅
```javascript
socket.subscribe('/topic/price/BTCUSDT', (message) => {
  console.log('价格更新:', JSON.parse(message.body));
});
```

#### 成交订阅
```javascript
socket.subscribe('/topic/trade/BTCUSDT', (message) => {
  console.log('成交更新:', JSON.parse(message.body));
});
```

## 消息队列

### 订单消息格式
```json
{
  "action": "CREATE",
  "orderNo": "O1634567890000001",
  "userId": 1001,
  "symbol": "BTCUSDT",
  "orderType": "LIMIT",
  "direction": "BUY",
  "price": "50000.00",
  "amount": "0.1",
  "timeInForce": 1
}
```

### 支持的操作
- **CREATE**: 创建新订单
- **CANCEL**: 取消订单
- **MODIFY**: 修改订单

## 性能指标

### 撮合性能
- **订单处理延迟**: < 1ms
- **并发处理能力**: 10,000+ TPS
- **订单簿容量**: 1,000,000+ 订单
- **内存占用**: < 2GB

### 系统性能
- **响应时间**: < 10ms
- **并发连接**: 10,000+
- **消息吞吐量**: 100,000+ msg/s
- **可用性**: 99.9%

## 监控指标

### JVM 指标
- 堆内存使用
- GC 频率和耗时
- 线程池状态
- CPU 使用率

### 业务指标
- 订单处理量
- 撮合成功率
- 平均延迟
- 错误率

### 系统指标
- 服务状态
- 数据库连接
- Redis 缓存
- 消息队列

## 配置说明

### 核心配置
```yaml
match:
  engine:
    buffer-size: 1024        # Disruptor 缓冲区大小
    thread-count: 4          # 处理线程数
    order-book-depth: 1000   # 订单簿深度
    trade-history-size: 1000 # 交易历史大小
    price-precision: 8       # 价格精度
    amount-precision: 8      # 数量精度
```

### JVM 优化
```bash
# 推荐JVM参数
-Xms2048m -Xmx4096m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=50
-XX:+UseStringDeduplication
-XX:+UseCompressedOops
-XX:+AggressiveOpts
```

## 最佳实践

### 性能优化
1. **合理配置线程数**: 根据CPU核心数配置
2. **调整缓冲区大小**: 根据业务量调整
3. **优化垃圾回收**: 使用G1垃圾回收器
4. **监控内存使用**: 避免内存溢出

### 高可用配置
1. **集群部署**: 多实例部署提高可用性
2. **负载均衡**: 使用Nginx进行负载均衡
3. **容灾备份**: 数据定期备份
4. **监控告警**: 设置监控告警规则

### 安全配置
1. **接口认证**: JWT Token 认证
2. **数据加密**: 敏感数据加密存储
3. **访问控制**: IP 白名单限制
4. **日志审计**: 完整的操作日志

## 故障排查

### 常见问题
1. **订单处理延迟**: 检查线程池和内存使用
2. **内存溢出**: 调整JVM参数和缓冲区大小
3. **消息堆积**: 检查消息队列和消费者配置
4. **连接超时**: 检查网络和服务状态

### 调试方法
1. **查看日志**: 分析详细日志信息
2. **监控指标**: 查看性能监控数据
3. **链路追踪**: 使用分布式链路追踪
4. **压力测试**: 模拟高并发场景测试

## 版本历史

### v1.0.0 (2025-09-18)
- 初始版本发布
- 实现基础撮合功能
- 集成消息队列
- 支持WebSocket推送

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码变更
4. 创建 Pull Request
5. 代码审查和合并

## 许可证

Apache License 2.0

## 联系方式

- 邮箱: dev@ppcex.com
- 文档: https://docs.ppcex.com
- 问题反馈: https://github.com/ppcex/match-service/issues