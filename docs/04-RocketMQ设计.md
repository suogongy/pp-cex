# Web3 CEX RocketMQ消息队列设计

## 1. 消息队列架构设计

### 1.1 设计目标
- **高可靠性**: 消息不丢失，支持事务消息
- **高性能**: 单机10万+ TPS，毫秒级延迟
- **顺序性**: 保证关键业务消息的顺序性
- **可扩展性**: 支持水平扩展，集群部署
- **可追踪性**: 完整的消息轨迹和监控

### 1.2 架构原则
- **异步解耦**: 系统间异步通信，降低耦合度
- **削峰填谷**: 应对高并发场景，平滑流量
- **最终一致性**: 通过消息保证数据最终一致
- **故障隔离**: 消息队列隔离，防止级联故障

## 2. RocketMQ架构

### 2.1 整体架构
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  应用层                                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 生产者1     │  │ 生产者2     │  │ 消费者1     │  │ 消费者2     │             │
│  │ Producer1   │  │ Producer2   │  │ Consumer1   │  │ Consumer2   │             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  NameServer集群                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                             │
│  │NameServer1 │  │NameServer2 │  │NameServer3 │                             │
│  └─────────────┘  └─────────────┘  └─────────────┘                             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  Broker集群                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │  Broker-a   │  │  Broker-b   │  │  Broker-c   │  │  Broker-d   │             │
│  │Master-Slave │  │Master-Slave │  │Master-Slave │  │Master-Slave │             │
│  │   Queue0-7  │  │   Queue0-7  │  │   Queue0-7  │  │   Queue0-7  │             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 部署架构
- **NameServer**: 3节点集群，高可用
- **Broker**: 4节点集群，主从部署
- **生产者**: 集群部署，负载均衡
- **消费者**: 集群部署，消息负载均衡

## 3. Topic设计

### 3.1 Topic规划
| Topic | 用途 | 消息类型 | 队列数 | 重要级别 |
|-------|------|----------|---------|----------|
| order-topic | 订单相关消息 | 顺序消息、事务消息 | 16 | 高 |
| asset-topic | 资产变动消息 | 顺序消息、事务消息 | 16 | 高 |
| trade-topic | 交易相关消息 | 普通消息 | 8 | 中 |
| notify-topic | 通知消息 | 广播消息 | 4 | 中 |
| risk-topic | 风控消息 | 普通消息 | 4 | 高 |
| wallet-topic | 钱包相关消息 | 事务消息 | 8 | 高 |
| market-topic | 行情数据消息 | 普通消息 | 32 | 中 |
| system-topic | 系统消息 | 广播消息 | 4 | 低 |

### 3.2 消息体设计
```json
{
  "header": {
    "message_id": "msg_123456789",
    "topic": "order-topic",
    "tags": "ORDER_CREATE",
    "keys": "order_123",
    "timestamp": 1640995200000,
    "producer_group": "order-producer",
    "consumer_group": "order-consumer",
    "retry_times": 0,
    "trace_id": "trace_123456789"
  },
  "body": {
    "order_id": 123,
    "user_id": 456,
    "symbol": "BTCUSDT",
    "order_type": 1,
    "direction": 1,
    "price": "50000.00",
    "amount": "0.001",
    "status": 1
  }
}
```

## 4. 核心业务消息设计

### 4.1 订单消息 (order-topic)

#### 4.1.1 订单创建消息
```json
{
  "header": {
    "message_id": "msg_123456789",
    "topic": "order-topic",
    "tags": "ORDER_CREATE",
    "keys": "order_123",
    "timestamp": 1640995200000
  },
  "body": {
    "order_id": 123,
    "order_no": "ORD20240101001",
    "user_id": 456,
    "symbol": "BTCUSDT",
    "order_type": 1,
    "direction": 1,
    "price": "50000.00",
    "amount": "0.001",
    "status": 1,
    "time_in_force": 1,
    "source": 1
  }
}
```

#### 4.1.2 订单成交消息
```json
{
  "header": {
    "message_id": "msg_123456790",
    "topic": "order-topic",
    "tags": "ORDER_TRADE",
    "keys": "order_123",
    "timestamp": 1640995201000
  },
  "body": {
    "order_id": 123,
    "trade_id": 789,
    "trade_no": "TRD20240101001",
    "symbol": "BTCUSDT",
    "price": "50000.00",
    "amount": "0.001",
    "executed_amount": "0.001",
    "executed_value": "50.00000000",
    "fee": "0.00100000",
    "trade_time": 1640995201000
  }
}
```

### 4.2 资产消息 (asset-topic)

#### 4.2.1 资产变动消息
```json
{
  "header": {
    "message_id": "msg_123456791",
    "topic": "asset-topic",
    "tags": "ASSET_CHANGE",
    "keys": "asset_456_BTC",
    "timestamp": 1640995202000
  },
  "body": {
    "user_id": 456,
    "coin_id": "BTC",
    "coin_name": "Bitcoin",
    "business_type": 3, // 1-充值 2-提现 3-买入 4-卖出
    "amount": "-0.001",
    "balance_before": "1.00000000",
    "balance_after": "0.99900000",
    "fee": "0.00100000",
    "ref_order_no": "ORD20240101001",
    "change_time": 1640995202000
  }
}
```

### 4.3 交易消息 (trade-topic)

#### 4.3.1 撮合结果消息
```json
{
  "header": {
    "message_id": "msg_123456792",
    "topic": "trade-topic",
    "tags": "MATCH_RESULT",
    "keys": "match_789",
    "timestamp": 1640995203000
  },
  "body": {
    "match_id": 789,
    "symbol": "BTCUSDT",
    "maker_order_id": 123,
    "taker_order_id": 456,
    "maker_user_id": 111,
    "taker_user_id": 222,
    "price": "50000.00",
    "amount": "0.001",
    "value": "50.00000000",
    "match_time": 1640995203000
  }
}
```

## 5. 事务消息设计

### 5.1 事务消息流程
```
1. 生产者发送半消息
2. 生产者执行本地事务
3. 生产者提交或回滚消息
4. Broker检查事务状态
5. 消费者消费消息
```

### 5.2 订单创建事务消息
```java
@Component
public class OrderTransactionListener implements TransactionListener {

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            // 解析消息
            OrderDTO order = parseOrderMessage(msg);

            // 执行本地事务 - 创建订单
            orderService.createOrder(order);

            // 创建资产冻结记录
            assetService.freezeAsset(order);

            return LocalTransactionState.COMMIT_MESSAGE;
        } catch (Exception e) {
            log.error("订单创建事务执行失败", e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(Message msg) {
        try {
            // 检查订单是否存在
            String orderNo = msg.getKeys();
            Order order = orderService.getByOrderNo(orderNo);

            if (order != null) {
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                return LocalTransactionState.UNKNOW;
            }
        } catch (Exception e) {
            log.error("检查订单事务状态失败", e);
            return LocalTransactionState.UNKNOW;
        }
    }
}
```

### 5.3 资产变动事务消息
```java
@Component
public class AssetTransactionListener implements TransactionListener {

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            // 解析消息
            AssetChangeDTO assetChange = parseAssetMessage(msg);

            // 执行本地事务 - 资产变动
            assetService.changeAsset(assetChange);

            // 记录资金流水
            financialService.recordFlow(assetChange);

            return LocalTransactionState.COMMIT_MESSAGE;
        } catch (Exception e) {
            log.error("资产变动事务执行失败", e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(Message msg) {
        try {
            // 检查资产流水是否存在
            String flowNo = msg.getKeys();
            FinancialFlow flow = financialService.getByFlowNo(flowNo);

            if (flow != null) {
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                return LocalTransactionState.UNKNOW;
            }
        } catch (Exception e) {
            log.error("检查资产事务状态失败", e);
            return LocalTransactionState.UNKNOW;
        }
    }
}
```

## 6. 顺序消息设计

### 6.1 顺序消息发送
```java
@Service
public class AssetMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendAssetChangeMessage(AssetChangeDTO assetChange) {
        // 构建消息
        Message<AssetChangeDTO> message = MessageBuilder
            .withPayload(assetChange)
            .setHeader("message_id", UUID.randomUUID().toString())
            .setHeader("keys", assetChange.getFlowNo())
            .setHeader("timestamp", System.currentTimeMillis())
            .build();

        // 按用户ID发送到同一队列，保证顺序性
        rocketMQTemplate.syncSendOrderly(
            "asset-topic:ASSET_CHANGE",
            message,
            String.valueOf(assetChange.getUserId())
        );
    }
}
```

### 6.2 顺序消息消费
```java
@Component
@RocketMQMessageListener(
    topic = "asset-topic",
    selectorExpression = "ASSET_CHANGE",
    consumerGroup = "asset-consumer",
    messageModel = MessageModel.CLUSTERING,
    consumeMode = ConsumeMode.ORDERLY
)
public class AssetMessageConsumer implements RocketMQListener<Message<AssetChangeDTO>> {

    @Override
    public void onMessage(Message<AssetChangeDTO> message) {
        try {
            AssetChangeDTO assetChange = message.getPayload();

            // 处理资产变动
            assetService.processAssetChange(assetChange);

            // 确认消息
            log.info("资产变动消息处理成功: {}", assetChange.getFlowNo());
        } catch (Exception e) {
            log.error("资产变动消息处理失败", e);
            throw new RuntimeException("消息处理失败");
        }
    }
}
```

## 7. 延迟消息设计

### 7.1 订单超时取消
```java
@Service
public class OrderMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendOrderTimeoutMessage(Order order) {
        // 构建消息
        Message<Order> message = MessageBuilder
            .withPayload(order)
            .setHeader("message_id", UUID.randomUUID().toString())
            .setHeader("keys", order.getOrderNo())
            .setHeader("timestamp", System.currentTimeMillis())
            .build();

        // 发送延迟消息，30分钟后触发
        rocketMQTemplate.syncSend(
            "order-topic:ORDER_TIMEOUT",
            message,
            3000, // 超时时间
            18    // 延迟级别 18 = 30分钟
        );
    }
}
```

### 7.2 提现审核超时
```java
@Service
public class WithdrawMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendWithdrawAuditTimeoutMessage(WithdrawRecord withdraw) {
        // 构建消息
        Message<WithdrawRecord> message = MessageBuilder
            .withPayload(withdraw)
            .setHeader("message_id", UUID.randomUUID().toString())
            .setHeader("keys", withdraw.getWithdrawNo())
            .setHeader("timestamp", System.currentTimeMillis())
            .build();

        // 发送延迟消息，24小时后触发
        rocketMQTemplate.syncSend(
            "wallet-topic:WITHDRAW_AUDIT_TIMEOUT",
            message,
            3000, // 超时时间
            19    // 延迟级别 19 = 1小时
        );
    }
}
```

## 8. 消息消费设计

### 8.1 消费者配置
```yaml
rocketmq:
  name-server: rocketmq-nameserver1:9876;rocketmq-nameserver2:9876;rocketmq-nameserver3:9876
  producer:
    group: order-producer
    send-message-timeout: 3000
    retry-times-when-send-failed: 3
  consumer:
    # 订单消费者
    order-consumer:
      group: order-consumer
      topic: order-topic
      selector-expression: "ORDER_CREATE || ORDER_TRADE || ORDER_TIMEOUT"
      consume-thread-min: 20
      consume-thread-max: 64
      pull-batch-size: 32
    # 资产消费者
    asset-consumer:
      group: asset-consumer
      topic: asset-topic
      selector-expression: "ASSET_CHANGE"
      consume-thread-min: 20
      consume-thread-max: 64
      pull-batch-size: 32
      consume-mode: ORDERLY
```

### 8.2 消息消费者
```java
@Component
@RocketMQMessageListener(
    topic = "order-topic",
    selectorExpression = "ORDER_CREATE || ORDER_TRADE || ORDER_TIMEOUT",
    consumerGroup = "order-consumer",
    messageModel = MessageModel.CLUSTERING
)
public class OrderMessageConsumer implements RocketMQListener<Message<OrderDTO>> {

    @Override
    public void onMessage(Message<OrderDTO> message) {
        try {
            OrderDTO order = message.getPayload();
            String tags = message.getHeaders().get("tags").toString();

            switch (tags) {
                case "ORDER_CREATE":
                    handleOrderCreate(order);
                    break;
                case "ORDER_TRADE":
                    handleOrderTrade(order);
                    break;
                case "ORDER_TIMEOUT":
                    handleOrderTimeout(order);
                    break;
                default:
                    log.warn("未知的订单消息类型: {}", tags);
            }
        } catch (Exception e) {
            log.error("订单消息处理失败", e);
            // 重试机制由RocketMQ自动处理
            throw new RuntimeException("消息处理失败");
        }
    }

    private void handleOrderCreate(OrderDTO order) {
        // 处理订单创建
        log.info("处理订单创建: {}", order.getOrderNo());
    }

    private void handleOrderTrade(OrderDTO order) {
        // 处理订单成交
        log.info("处理订单成交: {}", order.getOrderNo());
    }

    private void handleOrderTimeout(OrderDTO order) {
        // 处理订单超时
        log.info("处理订单超时: {}", order.getOrderNo());
        orderService.cancelOrder(order.getOrderNo());
    }
}
```

## 9. 消息监控与追踪

### 9.1 消息轨迹
```java
@Configuration
public class RocketMQTraceConfig {

    @Bean
    public TraceProducer traceProducer() {
        return new TraceProducer();
    }

    @Bean
    public TraceConsumer traceConsumer() {
        return new TraceConsumer();
    }
}
```

### 9.2 消息监控
```java
@Component
public class RocketMQMonitor {

    @Autowired
    private MQAdminExt mqAdminExt;

    public void monitorTopicStatus(String topic) {
        try {
            TopicStatsTable topicStats = mqAdminExt.examineTopicStats(topic);
            // 分析主题状态
            log.info("Topic {} 状态: {}", topic, topicStats);
        } catch (Exception e) {
            log.error("监控Topic状态失败", e);
        }
    }

    public void monitorConsumerProgress(String consumerGroup, String topic) {
        try {
            ConsumeStats consumeStats = mqAdminExt.examineConsumeStats(consumerGroup, topic);
            // 分析消费进度
            log.info("消费者组 {} 消费进度: {}", consumerGroup, consumeStats);
        } catch (Exception e) {
            log.error("监控消费进度失败", e);
        }
    }
}
```

## 10. 异常处理与重试

### 10.1 消息重试策略
```yaml
rocketmq:
  consumer:
    max-reconsume-times: 16
    suspend-current-queue-time-millis: 1000
    delay-level-when-next-consume: 0
```

### 10.2 死信队列处理
```java
@Component
@RocketMQMessageListener(
    topic = "%DLQ%order-consumer",
    consumerGroup = "dlq-order-consumer"
)
public class DLQMessageConsumer implements RocketMQListener<Message> {

    @Override
    public void onMessage(Message message) {
        try {
            // 处理死信消息
            log.warn("处理死信消息: {}", message.getKeys());

            // 记录到数据库
            dlqService.saveDLQMessage(message);

            // 发送告警
            alertService.sendAlert("订单消息处理失败", message.toString());
        } catch (Exception e) {
            log.error("处理死信消息失败", e);
        }
    }
}
```

## 11. 性能优化

### 11.1 批量消息发送
```java
@Service
public class BatchMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendBatchMessages(List<Message<?>> messages) {
        // 批量发送消息
        rocketMQTemplate.syncSend(
            "trade-topic:TRADE_BATCH",
            messages,
            5000, // 超时时间
            4     // 延迟级别
        );
    }
}
```

### 11.2 异步消息发送
```java
@Service
public class AsyncMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendAsyncMessage(Message<?> message) {
        // 异步发送消息
        rocketMQTemplate.asyncSend("market-topic:MARKET_DATA", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("消息发送成功: {}", sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("消息发送失败", e);
            }
        });
    }
}
```

## 12. 安全设计

### 12.1 消息加密
```java
@Component
public class MessageEncryptor {

    @Autowired
    private AESUtil aesUtil;

    public Message<?> encryptMessage(Message<?> message) {
        try {
            // 加密消息体
            String body = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            String encryptedBody = aesUtil.encrypt(body);

            // 重新构建消息
            return MessageBuilder
                .withPayload(encryptedBody.getBytes(StandardCharsets.UTF_8))
                .copyHeaders(message.getHeaders())
                .build();
        } catch (Exception e) {
            log.error("消息加密失败", e);
            return message;
        }
    }

    public Message<?> decryptMessage(Message<?> message) {
        try {
            // 解密消息体
            String encryptedBody = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            String body = aesUtil.decrypt(encryptedBody);

            // 重新构建消息
            return MessageBuilder
                .withPayload(body.getBytes(StandardCharsets.UTF_8))
                .copyHeaders(message.getHeaders())
                .build();
        } catch (Exception e) {
            log.error("消息解密失败", e);
            return message;
        }
    }
}
```

### 12.2 访问控制
```java
@Configuration
public class RocketMQSecurityConfig {

    @Bean
    public ACLClient aclClient() {
        return new ACLClient();
    }

    @Bean
    public AccessValidator accessValidator() {
        return new AccessValidator();
    }
}
```

## 13. 配置管理

### 13.1 Broker配置
```ini
# Broker配置
brokerName=broker-a
brokerClusterName=DefaultCluster
brokerId=0
deleteWhen=04
fileReservedTime=48
brokerRole=SYNC_MASTER
flushDiskType=SYNC_FLUSH

# 线程池配置
sendMessageThreadPoolNums=16
pullMessageThreadPoolNums=32
queryMessageThreadPoolNums=8

# 存储配置
mapedFileSizeCommitLog=1073741824
mapedFileSizeConsumeQueue=30000000
maxMessageSize=65536

# 网络配置
listenPort=10911
transferMsgByHeap=true
```

### 13.2 NameServer配置
```ini
# NameServer配置
listenPort=9876
serverWorkerThreads=8
serverCallbackExecutorThreads=0
serverSelectorThreads=4
serverOnewaySemaphoreValue=256
serverSemaphoreValue=64
```

## 14. 监控告警

### 14.1 关键指标监控
- **消息堆积量**: 监控队列消息堆积情况
- **消息延迟**: 监控消息发送和消费延迟
- **消费进度**: 监控消费者消费进度
- **TPS**: 监控消息处理吞吐量
- **错误率**: 监控消息处理错误率

### 14.2 告警规则
- **消息堆积**: 超过1000条告警
- **消息延迟**: 超过1分钟告警
- **消费延迟**: 超过5分钟告警
- **TPS下降**: 超过50%告警
- **错误率上升**: 超过1%告警