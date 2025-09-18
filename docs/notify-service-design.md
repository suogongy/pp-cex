# Notify-Service 技术设计文档

## 1. 系统概述

### 1.1 设计目标
Notify-Service是Web3 CEX系统中的通知服务，负责处理各种业务场景的通知需求，包括：
- 订单状态变更通知
- 交易成交通知
- 资产变动通知
- 安全告警通知
- 系统维护通知

### 1.2 技术架构
- **框架**: Spring Boot 3.x + Spring Cloud Alibaba
- **消息队列**: RocketMQ
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **服务注册**: Nacos
- **API文档**: Knife4j

## 2. 系统架构设计

### 2.1 整体架构
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  外部系统                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 交易服务     │  │ 钱包服务     │  │ 用户服务     │  │ 风控服务     │             │
│  │Trade Service│  │Wallet Service│  │User Service │  │Risk Service │             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  Notify-Service                                   │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                    消息队列层 (RocketMQ)                                      │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                         │ │
│  │  │notify-topic │  │order-topic  │  │asset-topic  │                         │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                         │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                        │                                          │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                    业务逻辑层                                                  │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                         │ │
│  │  │ 消息消费     │  │ 通知处理     │  │ 发送管理     │                         │ │
│  │  │Consumer     │  │Service      │  │Sender       │                         │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                         │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                        │                                          │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                    数据持久层                                                  │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                         │ │
│  │  │ MySQL       │  │ Redis       │  │ 模板管理     │                         │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                         │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                        │                                          │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                    通知发送层                                                  │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                         │ │
│  │  │ 邮件发送     │  │ 短信发送     │  │ 站内信发送   │                         │ │
│  │  │Email Sender │  │SMS Sender   │  │InApp Sender │                         │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                         │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 核心模块
- **消息消费模块**: 接收其他业务系统的通知需求
- **通知处理模块**: 处理通知逻辑，包括模板渲染、用户偏好检查等
- **发送管理模块**: 管理各种通知渠道的发送
- **配置管理模块**: 管理通知配置和模板
- **监控统计模块**: 监控通知发送状态和统计

## 3. 数据模型设计

### 3.1 核心数据表

#### 3.1.1 通知配置表 (notify_config)
存储各种通知渠道的配置信息。

| 字段名 | 类型 | 描述 | 约束 |
|--------|------|------|------|
| id | bigint | 主键ID | PK |
| config_type | tinyint | 配置类型 | 1-邮件 2-短信 3-站内信 4-推送 5-Webhook |
| config_name | varchar | 配置名称 | NOT NULL |
| config_key | varchar | 配置键 | NOT NULL, UNIQUE |
| config_value | text | 配置值(JSON) | NOT NULL |
| status | tinyint | 状态 | 1-启用 2-禁用 |
| description | varchar | 描述 | |
| create_time | datetime | 创建时间 | NOT NULL |
| update_time | datetime | 更新时间 | NOT NULL |

#### 3.1.2 通知模板表 (notify_template)
存储通知模板信息。

| 字段名 | 类型 | 描述 | 约束 |
|--------|------|------|------|
| id | bigint | 主键ID | PK |
| template_code | varchar | 模板编码 | NOT NULL |
| template_name | varchar | 模板名称 | NOT NULL |
| template_type | tinyint | 模板类型 | 1-邮件 2-短信 3-站内信 4-推送 |
| template_content | text | 模板内容 | NOT NULL |
| template_vars | text | 模板变量(JSON) | |
| language | varchar | 语言 | NOT NULL, DEFAULT 'zh-CN' |
| status | tinyint | 状态 | 1-启用 2-禁用 |
| description | varchar | 描述 | |
| create_time | datetime | 创建时间 | NOT NULL |
| update_time | datetime | 更新时间 | NOT NULL |

#### 3.1.3 通知记录表 (notify_record)
存储通知发送记录。

| 字段名 | 类型 | 描述 | 约束 |
|--------|------|------|------|
| id | bigint | 主键ID | PK |
| notify_no | varchar | 通知编号 | NOT NULL, UNIQUE |
| business_type | tinyint | 业务类型 | 1-订单 2-交易 3-资产 4-安全 5-系统 |
| notify_type | tinyint | 通知类型 | 1-邮件 2-短信 3-站内信 4-推送 |
| user_id | bigint | 用户ID | |
| recipient | varchar | 接收者 | NOT NULL |
| title | varchar | 标题 | |
| content | text | 内容 | NOT NULL |
| template_code | varchar | 模板编码 | |
| template_vars | text | 模板变量(JSON) | |
| status | tinyint | 状态 | 1-待发送 2-发送中 3-已发送 4-发送失败 |
| send_count | int | 发送次数 | NOT NULL, DEFAULT 0 |
| max_retry | int | 最大重试次数 | NOT NULL, DEFAULT 3 |
| next_retry_time | datetime | 下次重试时间 | |
| error_msg | text | 错误信息 | |
| send_time | datetime | 发送时间 | |
| create_time | datetime | 创建时间 | NOT NULL |
| update_time | datetime | 更新时间 | NOT NULL |

#### 3.1.4 用户通知偏好表 (user_notify_preference)
存储用户通知偏好设置。

| 字段名 | 类型 | 描述 | 约束 |
|--------|------|------|------|
| id | bigint | 主键ID | PK |
| user_id | bigint | 用户ID | NOT NULL |
| notify_type | tinyint | 通知类型 | NOT NULL |
| business_type | tinyint | 业务类型 | NOT NULL |
| enabled | tinyint | 是否启用 | NOT NULL, DEFAULT 1 |
| contact_info | varchar | 联系方式 | |
| create_time | datetime | 创建时间 | NOT NULL |
| update_time | datetime | 更新时间 | NOT NULL |

### 3.2 索引设计
- **notify_record表**:
  - `idx_business_type`: 业务类型索引
  - `idx_notify_type`: 通知类型索引
  - `idx_user_id`: 用户ID索引
  - `idx_status`: 状态索引
  - `idx_create_time`: 创建时间索引
  - `idx_next_retry_time`: 重试时间索引

## 4. 业务流程设计

### 4.1 通知发送流程
```
1. 接收通知请求
   ↓
2. 验证用户通知偏好
   ↓
3. 获取通知模板
   ↓
4. 处理模板变量
   ↓
5. 创建通知记录
   ↓
6. 异步发送通知
   ↓
7. 更新发送状态
   ↓
8. 记录统计信息
```

### 4.2 消息消费流程
```
1. 接收RocketMQ消息
   ↓
2. 解析消息内容
   ↓
3. 调用通知服务
   ↓
4. 处理发送结果
   ↓
5. 消息确认
```

### 4.3 失败重试流程
```
1. 发送失败
   ↓
2. 更新重试次数
   ↓
3. 计算下次重试时间
   ↓
4. 等待重试时间
   ↓
5. 重新发送
   ↓
6. 达到最大重试次数则标记为失败
```

## 5. 接口设计

### 5.1 REST API接口

#### 5.1.1 发送通知
```
POST /api/notify/send
Content-Type: application/json

{
    "businessType": 1,
    "notifyType": 1,
    "userId": 123,
    "recipient": "user@example.com",
    "templateCode": "ORDER_CREATE",
    "templateVars": {
        "userName": "张三",
        "orderNo": "ORD20240101001",
        "amount": "0.001"
    },
    "language": "zh-CN"
}
```

#### 5.1.2 批量发送通知
```
POST /api/notify/batch-send
Content-Type: application/json

[
    {
        "businessType": 1,
        "notifyType": 1,
        "userId": 123,
        "recipient": "user@example.com",
        "templateCode": "ORDER_CREATE"
    },
    {
        "businessType": 2,
        "notifyType": 2,
        "userId": 123,
        "recipient": "13800138000",
        "templateCode": "TRADE_SUCCESS"
    }
]
```

#### 5.1.3 重试通知
```
POST /api/notify/retry/{notifyNo}
```

#### 5.1.4 查询通知状态
```
GET /api/notify/status/{notifyNo}
```

### 5.2 消息队列接口

#### 5.2.1 消息格式
```json
{
    "header": {
        "message_id": "msg_123456789",
        "topic": "notify-topic",
        "tags": "NOTIFY_SEND",
        "keys": "order_123",
        "timestamp": 1640995200000
    },
    "body": {
        "businessType": 1,
        "notifyType": 1,
        "userId": 123,
        "recipient": "user@example.com",
        "templateCode": "ORDER_CREATE",
        "templateVars": {
            "userName": "张三",
            "orderNo": "ORD20240101001"
        }
    }
}
```

## 6. 技术实现

### 6.1 核心类设计

#### 6.1.1 NotifyService
核心通知服务，负责处理通知发送逻辑。

```java
public interface NotifyService {
    NotifyResponseDTO sendNotify(NotifyRequestDTO requestDTO);
    List<NotifyResponseDTO> batchSendNotify(List<NotifyRequestDTO> requestDTOList);
    boolean retryNotify(String notifyNo);
    Integer getNotifyStatus(String notifyNo);
}
```

#### 6.1.2 NotifySender
通知发送器接口，支持多种发送方式。

```java
public interface NotifySender {
    boolean send(NotifyRecord record);
    Integer getType();
}
```

#### 6.1.3 NotifySenderFactory
发送器工厂，根据通知类型选择对应的发送器。

### 6.2 模板处理
使用正则表达式处理模板变量：
```
模板: 您好${userName}，您的订单${orderNo}已创建
变量: {"userName": "张三", "orderNo": "ORD20240101001"}
结果: 您好张三，您的订单ORD20240101001已创建
```

### 6.3 异步发送
使用多线程实现异步发送，提高系统响应速度。

### 6.4 用户偏好检查
在发送前检查用户是否启用了对应的通知类型。

## 7. 配置管理

### 7.1 Nacos配置
- 服务发现配置
- 配置中心配置
- 限流规则配置

### 7.2 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notify_db
    username: root
    password: password
```

### 7.3 Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 7.4 RocketMQ配置
```yaml
spring:
  rocketmq:
    name-server: localhost:9876
    producer:
      group: notify-producer
```

## 8. 性能优化

### 8.1 缓存策略
- 模板缓存：缓存常用模板，减少数据库查询
- 配置缓存：缓存通知配置，提高访问速度
- 用户偏好缓存：缓存用户偏好设置

### 8.2 连接池优化
- 数据库连接池：合理配置连接池大小
- Redis连接池：优化Redis连接配置

### 8.3 异步处理
- 异步发送：使用线程池异步发送通知
- 批量处理：支持批量发送通知

## 9. 监控与告警

### 9.1 监控指标
- 发送成功率
- 发送延迟
- 失败率
- 系统负载

### 9.2 告警规则
- 发送失败率超过5%
- 发送延迟超过1分钟
- 系统负载超过80%

## 10. 安全设计

### 10.1 数据加密
- 敏感信息加密存储
- 传输加密

### 10.2 访问控制
- 接口访问控制
- 数据访问控制

### 10.3 审计日志
- 操作日志记录
- 访问日志记录

## 11. 扩展性设计

### 11.1 插件化架构
- 支持新增通知类型
- 支持新增模板引擎

### 11.2 配置化
- 模板配置化
- 渠道配置化

## 12. 部署设计

### 12.1 Docker部署
```dockerfile
FROM openjdk:17-jre-slim
COPY target/notify-service-1.0.0.jar app.jar
EXPOSE 8007
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 12.2 Kubernetes部署
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notify-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: notify-service
  template:
    metadata:
      labels:
        app: notify-service
    spec:
      containers:
      - name: notify-service
        image: notify-service:1.0.0
        ports:
        - containerPort: 8007
```

## 13. 测试策略

### 13.1 单元测试
- 服务层测试
- 发送器测试
- 工具类测试

### 13.2 集成测试
- 接口测试
- 消息队列测试
- 数据库测试

### 13.3 性能测试
- 并发发送测试
- 压力测试
- 负载测试

## 14. 常见问题处理

### 14.1 发送失败处理
- 重试机制
- 死信队列处理
- 告警通知

### 14.2 模板处理失败
- 默认模板处理
- 错误日志记录
- 人工干预

### 14.3 系统故障处理
- 降级处理
- 故障转移
- 数据恢复

## 15. 未来规划

### 15.1 功能扩展
- 支持更多通知渠道
- 支持多语言模板
- 支持定时通知

### 15.2 性能优化
- 分布式发送
- 消息分区
- 缓存优化

### 15.3 监控增强
- 实时监控
- 智能告警
- 性能分析