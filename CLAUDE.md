# Web3 CEX 学习项目

## 注意事项  
- 在claude分析处理的过程中，严格使用中文
- 详细文档请查看[产品需求文档.md](docs/产品需求文档.md)

## 项目概述
这是一个简化版的Web3中心化交易所(CEX)学习项目，基于Spring Cloud Alibaba和Next.js技术栈。

## 技术栈

### 后端
- **框架**: Spring Boot 3.x + Spring Cloud Alibaba 2022.x
- **服务注册发现**: Nacos 2.2.x
- **配置管理**: Nacos Config
- **服务网关**: Spring Cloud Gateway
- **熔断限流**: Sentinel
- **分布式事务**: Seata
- **消息队列**: RocketMQ 4.9.x
- **缓存**: Redis 7.x
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus
- **区块链**: Web3.js

### 前端
- **框架**: Next.js 14 + TypeScript
- **UI框架**: Ant Design + Tailwind CSS
- **状态管理**: Zustand
- **图表库**: ECharts
- **实时通信**: WebSocket

### 项目结构
```
pp-cex/
├── backend/                    # 后端微服务
│   ├── user-service/          # 用户服务
│   ├── trade-service/         # 交易服务
│   ├── wallet-service/        # 钱包服务
│   ├── finance-service/       # 财务服务
│   ├── gateway-service/       # 网关服务
│   └── common/                # 公共模块
├── frontend/                  # 前端应用
│   ├── pages/                 # 页面组件
│   ├── components/            # 通用组件
│   ├── hooks/                 # 自定义Hooks
│   ├── store/                 # 状态管理
│   └── utils/                 # 工具函数
├── docker/                    # Docker配置
├── sql/                       # 数据库脚本
└── docs/                      # 项目文档
```

## 开发指南

### 环境要求
- JDK 17+
- Node.js 18+
- Docker 20+
- MySQL 8.0+
- Redis 7+
- RocketMQ 4.9.x+

### 快速启动
```bash
# 启动基础设施服务
cd backend && docker-compose -f docker/docker-compose.yml up -d

# 或使用传统方式启动后端服务
cd backend && mvn spring-boot:run

# 启动前端应用
cd frontend && npm run dev
```

### 开发规范
- 代码风格：遵循阿里巴巴Java开发手册
- API规范：RESTful + OpenAPI 3.0
- 数据库：使用Flyway管理数据库版本
- 测试：单元测试覆盖率 > 80%

## JWT认证架构

### JWT实现架构
项目中采用**两套JWT实现**的架构设计，分别服务于不同的场景：

#### 1. Common模块JWT服务
**文件位置**: `backend/common/src/main/java/com/ppcex/common/service/JwtService.java`

**适用场景**: 传统Spring MVC微服务（user-service, trade-service, wallet-service等）

**特点**:
- 基于配置属性的条件注入：`@ConditionalOnProperty(name = "cex.jwt.enabled", havingValue = "true")`
- 配置前缀：`cex.jwt`
- 支持访问令牌和刷新令牌
- 功能完备的JWT工具类

**配置示例**:
```yaml
cex:
  jwt:
    enabled: true
    secret: your-jwt-secret-key-at-least-32-bytes-long-for-security
    expiration: 86400000  # 24小时
    refresh-expiration: 604800000  # 7天
    issuer: PPCEX
```

#### 2. Gateway-service JWT服务
**文件位置**: `backend/gateway-service/src/main/java/com/ppcex/gateway/service/JwtTokenService.java`

**适用场景**: 基于WebFlux的响应式网关服务

**特点**:
- 集成Redis缓存和会话管理
- 支持令牌黑名单机制
- 专门为网关设计的高性能认证服务
- 统一配置前缀：`cex.jwt`

**增强功能**:
- 用户会话管理
- 令牌黑名单机制
- 分布式会话同步
- 响应式编程支持

### 统一配置管理
两个实现都使用统一的配置前缀`cex.jwt`，确保配置的一致性：

```yaml
cex:
  jwt:
    enabled: true
    secret: your-jwt-secret-key-at-least-32-bytes-long-for-security
    expiration: 86400000  # 24小时过期时间
    refresh-expiration: 604800000  # 7天刷新token过期时间
    issuer: PPCEX
```

### 使用建议
- **微服务层**: 使用common模块的JwtService
- **网关层**: 使用gateway-service的JwtTokenService
- **配置**: 统一使用`cex.jwt`前缀
- **密钥**: 生产环境务必使用强密钥并妥善保管

## 核心功能模块

### 1. 用户系统
- 用户注册登录
- KYC认证
- 资产管理
- 安全设置

### 2. 交易系统
- 币币交易
- 订单管理
- 撮合引擎
- 实时行情
- **RocketMQ事务消息**: 保证订单创建一致性
- **延迟消息**: 处理订单超时取消

### 3. 钱包系统
- 多币种钱包
- 充值提现
- 冷热钱包分离
- 资产安全
- **RocketMQ顺序消息**: 确保资产变动顺序性
- **事务消息**: 保证资产变动原子性

### 4. 财务系统
- 资金流水
- 财务统计
- 风控管理
- 合规审核
- **消息轨迹**: 完整的资金流水追踪
- **实时监控**: 异常交易实时告警

## 重要文件说明
- `产品需求文档.md` - 详细的产品需求规格
- `sql/` - 数据库初始化脚本
- `docker-compose.yml` - 容器编排配置
- `README.md` - 项目部署说明

## 开发注意事项
- 这是一个学习项目，不要用于生产环境
- 资产安全需要特别注意，建议使用测试网络
- 遵守当地的金融监管法规
- 注意保护用户隐私和数据安全

## 构建和部署
```bash
# 构建后端
cd backend && mvn clean package

# 构建前端
cd frontend && npm run build

# 使用Docker部署
docker-compose up -d
```

## 学习资源
- Spring Cloud Alibaba官方文档
- Next.js官方文档
- Web3.js文档
- 区块链交易原理
- RocketMQ官方文档
- 分布式事务处理模式

## 项目特色
- 完整的CEX系统架构
- 实时交易撮合
- 区块链集成
- 微服务架构
- 前后端分离
- **RocketMQ消息中间件**: 金融级消息可靠性
- **分布式事务**: Seata + RocketMQ事务消息

## 常见问题
- Q: 如何添加新的交易对？
- A: 在trade-service中配置交易对信息
- Q: 如何集成新的区块链？
- A: 在wallet-service中添加区块链适配器
- Q: 如何优化撮合引擎性能？
- A: 使用内存数据库和并发队列优化
- Q: RocketMQ如何保证消息不丢失？
- A: 配置同步刷盘、消息持久化、重试机制
- Q: 如何处理消息重复消费？
- A: 使用消息唯一ID和幂等性设计

## 联系方式
如有问题请查看项目文档或提交Issue。
- 严禁自己注册用户进行操作，避免生成垃圾数据