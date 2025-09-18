# Notify-Service 通知服务

## 服务概述

Notify-Service是Web3 CEX系统中的通知服务，负责处理各种业务场景的通知需求，包括订单状态变更、交易成交、资产变动、安全告警等通知。

## 功能特性

- 多渠道通知支持：邮件、短信、站内信、推送、Webhook
- 模板化通知：支持动态模板变量替换
- 用户偏好管理：用户可自定义通知接收偏好
- 异步发送：提高系统响应速度
- 失败重试：智能重试机制
- 监控统计：实时监控通知发送状态
- 消息队列：基于RocketMQ的消息消费

## 技术架构

- **框架**: Spring Boot 3.x + Spring Cloud Alibaba
- **消息队列**: RocketMQ
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **服务注册**: Nacos
- **API文档**: Knife4j

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7+
- RocketMQ 4.9.x
- Nacos 2.2.x

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p < ../../sql/notify-service-init.sql
```

### 3. 配置文件修改

修改 `src/main/resources/application.yml` 中的数据库连接和邮件配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notify_db
    username: root
    password: your_password
  mail:
    host: smtp.gmail.com
    username: your-email@gmail.com
    password: your-app-password
```

### 4. 编译打包

```bash
mvn clean package -DskipTests
```

### 5. 启动服务

```bash
# 使用启动脚本
./start.sh

# 或直接运行
java -jar target/notify-service-1.0.0.jar --spring.profiles.active=dev
```

### 6. 访问服务

服务启动后，可以通过以下地址访问：

- 服务端口: 8007
- API文档: http://localhost:8007/doc.html

## API 接口

### 发送通知

```bash
curl -X POST http://localhost:8007/api/notify/send \
  -H "Content-Type: application/json" \
  -d '{
    "businessType": 1,
    "notifyType": 1,
    "userId": 123,
    "recipient": "user@example.com",
    "templateCode": "ORDER_CREATE",
    "templateVars": {
      "userName": "张三",
      "orderNo": "ORD20240101001",
      "amount": "0.001"
    }
  }'
```

### 批量发送通知

```bash
curl -X POST http://localhost:8007/api/notify/batch-send \
  -H "Content-Type: application/json" \
  -d '[
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
  ]'
```

### 查询通知状态

```bash
curl -X GET http://localhost:8007/api/notify/status/NTF1234567890
```

### 重试通知

```bash
curl -X POST http://localhost:8007/api/notify/retry/NTF1234567890
```

## 消息队列

### 接收消息

服务监听 `notify-topic` 主题，处理标签为 `NOTIFY_SEND` 的消息。

### 消息格式

```json
{
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
```

## 配置管理

### Nacos配置

服务配置存储在Nacos配置中心：

- `notify-service-config.yaml`: 服务配置
- `common-config.yaml`: 公共配置

### 数据库配置

通知配置和模板存储在数据库中，支持动态配置：

- `notify_config`: 通知渠道配置
- `notify_template`: 通知模板
- `user_notify_preference`: 用户偏好设置

## 监控指标

### 系统监控

- 服务状态: http://localhost:8007/actuator/health
- 应用信息: http://localhost:8007/actuator/info
- 性能指标: http://localhost:8007/actuator/metrics

### 业务监控

- 发送成功率
- 发送延迟
- 失败重试次数
- 各渠道发送统计

## 常见问题

### 1. 邮件发送失败

检查邮件配置：
- 确认邮件服务器地址和端口
- 确认用户名和密码正确
- 确认网络连接正常

### 2. 短信发送失败

检查短信服务配置：
- 确认短信服务商配置
- 确认API密钥正确
- 确认短信模板审核通过

### 3. 模板处理失败

检查模板配置：
- 确认模板语法正确
- 确认变量名称匹配
- 确认模板状态为启用

### 4. 消息消费失败

检查消息队列配置：
- 确认RocketMQ服务正常
- 确认Topic配置正确
- 确认消费者组配置正确

## 开发指南

### 添加新的通知渠道

1. 实现 `NotifySender` 接口
2. 在 `NotifySenderFactory` 中注册
3. 添加相应的枚举值
4. 创建配置和模板

### 添加新的业务类型

1. 在 `BusinessTypeEnum` 中添加新的枚举
2. 创建对应的模板
3. 更新用户偏好配置

### 扩展模板功能

1. 修改 `TemplateUtil` 类
2. 添加新的模板语法支持
3. 更新模板变量处理逻辑

## 部署说明

### Docker部署

```dockerfile
FROM openjdk:17-jre-slim
COPY target/notify-service-1.0.0.jar app.jar
EXPOSE 8007
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes部署

参考 `k8s/` 目录下的部署文件。

## 联系方式

如有问题，请联系开发团队。

## 许可证

Apache License 2.0