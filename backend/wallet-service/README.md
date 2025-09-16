# 钱包服务 (Wallet Service)

## 概述

钱包服务是Web3 CEX系统的核心组件之一，负责处理所有与数字货币钱包相关的功能，包括充值、提现、余额管理、区块链集成等。

## 主要功能

### 1. 多币种钱包管理
- 支持BTC、ETH、USDT等多种数字货币
- 热钱包地址生成和管理
- 钱包余额监控和告警
- 私钥安全存储（AES加密）

### 2. 充值功能
- 充值地址自动分配
- 区块链交易监控和确认
- 自动充值确认和入账
- 充值记录查询和管理

### 3. 提现功能
- 提现申请和审核流程
- 自动/手动提现处理
- 提现限额控制
- 提现手续费管理

### 4. 资产安全管理
- 钱包余额实时监控
- 资金流水完整记录
- 异常交易告警
- 风控规则执行

### 5. RocketMQ消息集成
- 事务消息保证资产变动原子性
- 顺序消息确保用户资产变动顺序性
- 延迟消息处理提现审核超时
- 完整的消息轨迹追踪

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.x + Spring Cloud Alibaba
- **数据库**: MySQL 8.0 + MyBatis Plus
- **缓存**: Redis 7.x
- **消息队列**: RocketMQ 4.9.x
- **服务注册**: Nacos 2.2.x
- **区块链**: Web3j

### 核心模块
- **Controller**: RESTful API接口层
- **Service**: 业务逻辑处理层
- **Mapper**: 数据访问层
- **Entity**: 数据实体层
- **Message**: 消息处理层
- **Utils**: 工具类层

## 数据库设计

### 核心表结构
- `wallet_address`: 钱包地址表
- `recharge_record`: 充值记录表
- `withdraw_record`: 提现记录表
- `coin_config`: 币种配置表
- `wallet_transfer`: 钱包内部转账表
- `wallet_monitor_log`: 钱包监控日志表

### 索引优化
- 主键索引：所有表设置主键
- 唯一索引：业务唯一字段（地址、交易哈希等）
- 普通索引：查询条件字段（用户ID、币种、状态等）
- 复合索引：多字段查询条件

## API接口

### 钱包地址管理
```
POST /api/v1/wallet/address - 创建钱包地址
```

### 充值管理
```
GET /api/v1/wallet/recharge/records - 查询充值记录
POST /api/v1/wallet/recharge - 创建充值记录
```

### 提现管理
```
GET /api/v1/wallet/withdraw/records - 查询提现记录
POST /api/v1/wallet/withdraw - 创建提现请求
POST /api/v1/wallet/withdraw/audit - 审核提现
GET /api/v1/wallet/withdraw/pending-audit - 查询待审核提现
```

### 资产查询
```
GET /api/v1/wallet/balance - 查询用户余额
GET /api/v1/wallet/balance/check - 检查用户余额
```

### 监控管理
```
POST /api/v1/wallet/monitor/balance - 监控钱包余额
POST /api/v1/wallet/monitor/blockchain - 监控区块链交易
GET /api/v1/wallet/health - 健康检查
```

## 消息队列设计

### Topic规划
- `wallet-topic`: 钱包相关消息
- `asset-topic`: 资产变动消息

### 消息类型
- `WALLET_RECHARGE`: 充值消息
- `WALLET_WITHDRAW`: 提现消息
- `WALLET_TRANSFER`: 转账消息
- `ASSET_CHANGE`: 资产变动消息

### 消息特性
- **事务消息**: 保证资产变动原子性
- **顺序消息**: 确保用户资产变动顺序性
- **延迟消息**: 处理提现审核超时
- **消息追踪**: 完整的消息轨迹

## 安全设计

### 数据安全
- 私钥AES加密存储
- 敏感数据脱敏显示
- 数据库访问权限控制
- 传输层SSL加密

### 业务安全
- 提现审核机制
- 余额验证和限制
- 异常交易监控
- 风控规则执行

### 系统安全
- JWT认证授权
- API接口签名
- 访问频率限制
- 操作日志审计

## 监控告警

### 业务监控
- 钱包余额监控
- 充值确认监控
- 提现处理监控
- 异常交易监控

### 系统监控
- 服务健康检查
- 性能指标监控
- 消息队列监控
- 数据库连接监控

### 告警规则
- 余额低于阈值告警
- 充值确认延迟告警
- 提现处理失败告警
- 系统异常告警

## 部署配置

### 环境要求
- Java 17+
- MySQL 8.0+
- Redis 7.0+
- RocketMQ 4.9+
- Nacos 2.2+

### 启动服务
```bash
# 构建项目
mvn clean package -DskipTests

# 启动服务
./scripts/start.sh

# 停止服务
./scripts/stop.sh
```

### Docker部署
```bash
# 构建镜像
docker build -t wallet-service:1.0.0 .

# 运行容器
docker run -d -p 8083:8083 --name wallet-service wallet-service:1.0.0
```

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wallet_db
    username: root
    password: password
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### RocketMQ配置
```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: wallet-producer
  consumer:
    wallet-consumer:
      group: wallet-consumer
```

## 开发指南

### 代码结构
```
backend/wallet-service/
├── src/main/java/com/ppcex/wallet/
│   ├── controller/     # 控制器
│   ├── service/       # 服务层
│   ├── entity/        # 实体类
│   ├── mapper/        # 数据访问层
│   ├── config/        # 配置类
│   ├── dto/           # 数据传输对象
│   ├── enum/          # 枚举类
│   ├── utils/         # 工具类
│   └── message/       # 消息处理
├── src/main/resources/
│   ├── mapper/        # MyBatis映射文件
│   ├── application.yml
│   └── bootstrap.yml
├── scripts/           # 启动脚本
├── Dockerfile
└── README.md
```

### 开发规范
- 遵循阿里巴巴Java开发手册
- 使用统一的代码格式化规则
- 编写完整的单元测试
- 添加详细的注释文档
- 遵循RESTful API设计规范

### 测试指南
- 单元测试覆盖率达到80%以上
- 集成测试覆盖主要业务流程
- 性能测试验证系统负载能力
- 安全测试验证系统安全性

## 维护文档

### 日常维护
- 定期检查钱包余额
- 监控充值确认状态
- 处理异常提现请求
- 清理过期日志文件

### 故障处理
- 查看服务日志
- 检查数据库连接
- 验证消息队列状态
- 重启异常服务实例

### 版本升级
- 备份数据库数据
- 停止当前服务
- 部署新版本
- 验证功能正常

## 常见问题

### Q: 如何添加新的币种？
A: 在coin_config表中添加币种配置，并更新区块链服务中的相应逻辑。

### Q: 如何调整提现手续费？
A: 修改coin_config表中的withdraw_fee字段，或调整withdraw_fee_rate。

### Q: 如何处理充值确认延迟？
A: 检查区块链节点状态，调整required_confirmations参数。

### Q: 如何监控钱包余额？
A: 使用monitor/balance接口或配置定时任务自动监控。

## 联系方式

如有问题请联系开发团队或提交Issue。