# Finance Service - 财务系统服务

## 概述

财务系统服务是Web3 CEX的核心组件之一，负责处理所有财务相关的业务逻辑，包括：

- 资金流水管理
- 财务报表生成
- 风控管理
- 资产统计
- 手续费收入统计

## 技术栈

- **框架**: Spring Boot 3.2.0 + Spring Cloud Alibaba 2022.0.4
- **数据库**: MySQL 8.0 + MyBatis Plus 3.5.5
- **缓存**: Redis 7.0
- **消息队列**: RocketMQ 4.9.4
- **服务治理**: Nacos 2.2.x + Sentinel

## 核心功能

### 1. 资金流水管理

- 支持多种业务类型的资金流水记录
- 实时计算用户资产余额
- 提供流水查询和统计功能
- 支持按时间范围统计业务量

### 2. 财务报表生成

- 自动生成日报表
- 支持历史报表查询
- 提供财务数据统计接口
- 支持数据可视化

### 3. 风控管理

- 实时风控检查
- 多维度风控规则（IP、频率、金额、地理位置等）
- 风险事件记录和追踪
- 自动化风控处理

### 4. 消息处理

- 集成RocketMQ消息队列
- 支持事务消息确保数据一致性
- 异步处理提高系统性能
- 消息轨迹追踪

## API接口

### 资金流水相关

```
POST   /api/v1/finance/flows                    - 创建资金流水
GET    /api/v1/finance/flows/user/{userId}      - 获取用户资金流水
GET    /api/v1/finance/flows/user/{userId}/recent - 获取用户最近流水
GET    /api/v1/finance/assets/user/{userId}/coin/{coinId} - 获取用户资产
```

### 统计相关

```
GET    /api/v1/finance/stats/fee-income          - 获取手续费收入
GET    /api/v1/finance/stats/volume              - 获取业务量
```

### 报表相关

```
POST   /api/v1/finance/reports/daily             - 生成日报表
GET    /api/v1/finance/reports/daily             - 获取日报表列表
GET    /api/v1/finance/reports/stats             - 获取财务统计
```

### 风控相关

```
POST   /api/v1/finance/risk/check                - 风控检查
POST   /api/v1/finance/risk/ip-blacklist/add    - 添加IP黑名单
POST   /api/v1/finance/risk/ip-blacklist/remove - 移除IP黑名单
```

## 消息队列

### Topic规划

- `asset-topic`: 资产变动消息
- `risk-topic`: 风控告警消息
- `finance-topic`: 财务相关消息

### 消息类型

- **事务消息**: 确保资产变动和资金流水的一致性
- **广播消息**: 风控告警通知
- **普通消息**: 财务报表生成

## 数据库设计

### 核心表结构

1. **financial_flow** - 资金流水表
2. **financial_daily_report** - 日报表
3. **risk_control** - 风控规则表
4. **risk_event** - 风险事件表

## 部署说明

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- RocketMQ 4.9.4+
- Nacos 2.2.x+

### 启动服务

```bash
# 编译
mvn clean package

# 启动
java -jar finance-service-1.0.0.jar --spring.profiles.active=dev
```

### 配置说明

- `bootstrap.yml`: 服务发现和配置中心配置
- `application.yml`: 应用配置
- 支持Nacos配置中心动态配置

## 监控指标

- 服务健康状态
- 消息处理性能
- 数据库连接池状态
- 缓存命中率
- API响应时间

## 安全特性

- 数据传输加密
- 敏感数据脱敏
- 操作日志记录
- 访问权限控制
- 风控规则引擎

## 扩展性设计

- 微服务架构，支持水平扩展
- 分库分表策略
- 读写分离
- 缓存优化
- 消息队列削峰填谷