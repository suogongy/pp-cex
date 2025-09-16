# CEX 后端项目

> 🏗️ 基于 Spring Cloud Alibaba 的微服务架构中心化交易平台后端

## 📖 项目概述

本项目是 CEX 中心化交易平台的后端系统，采用 Spring Cloud Alibaba 微服务架构，实现了完整的加密货币交易业务逻辑。项目专注于学习目的，通过微服务架构帮助开发者深入理解 CEX 系统的核心原理和实现。

### 🎯 架构设计理念

- **微服务架构**：服务拆分，独立部署和扩展
- **高可用性**：服务注册发现、负载均衡、熔断降级
- **高并发处理**：分布式缓存、消息队列、读写分离
- **安全可靠**：身份认证、权限控制、数据加密

## 🛠️ 技术栈

### 核心框架
- **Spring Boot 3.2.0** - 应用开发框架
- **Spring Cloud Alibaba 2022.0.0.0** - 微服务套件
- **Spring Cloud 2022.0.4** - 云原生应用开发
- **Java 17** - 编程语言

### 微服务组件
- **Nacos 2.2.3** - 服务注册与配置中心
- **Sentinel 1.8.6** - 流量控制和熔断降级
- **Seata 1.6.1** - 分布式事务解决方案
- **RocketMQ 4.9.4** - 分布式消息队列
- **Dubbo 3.2.0** - 高性能 RPC 框架

### 数据存储
- **MySQL 8.0** - 主数据库（主从复制）
- **Redis 7.0** - 分布式缓存
- **MongoDB 6.0** - 文档存储（可选）

### 数据访问
- **MyBatis Plus 3.5.4** - ORM 框架
- **Druid 1.2.18** - 数据库连接池
- **ShardingSphere 5.3.2** - 分库分表中间件

### 安全框架
- **Spring Security 6.1** - 安全框架
- **JWT 0.11.5** - JSON Web Token
- **OAuth2 1.0** - 开放授权标准
- **BCrypt** - 密码加密

### 监控运维
- **Spring Boot Actuator** - 应用监控
- **Prometheus + Grafana** - 监控告警
- **ELK Stack** - 日志分析
- **SkyWalking** - 分布式链路追踪

### 开发工具
- **Maven 3.8+** - 项目构建工具
- **Docker** - 容器化部署
- **Jenkins** - CI/CD 持续集成
- **Git** - 版本控制

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **Docker 20.10+**
- **Docker Compose 2.0+**
- **Node.js 18+**（前端开发）

### 基础设施启动

```bash
# 进入后端项目目录
cd backend

# 启动基础设施（MySQL、Redis、RocketMQ、Nacos等）
docker-compose -f docker-compose.yml up -d

# 等待基础设施启动完成（约2-3分钟）
# 检查服务状态
docker-compose -f docker-compose.yml ps
```

### 开发环境启动

```bash
# 启动开发环境
docker-compose -f docker-compose.dev.yml up -d

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f

# 停止服务
docker-compose -f docker-compose.dev.yml down
```

### 生产环境启动

```bash
# 启动生产环境
docker-compose -f docker-compose.prod.yml up -d

# 或者使用项目部署脚本
cd ..
./scripts/deploy.sh prod up
```

### 本地开发

```bash
# 1. 启动基础设施
docker-compose -f docker-compose.yml up -d

# 2. 编译公共模块
cd common
mvn clean install

# 3. 启动各个微服务
cd user-service
mvn spring-boot:run

# 在其他终端启动其他服务...
cd ../trade-service
mvn spring-boot:run
```

## 📁 项目结构

```
backend/
├── common/                      # 公共模块
│   ├── src/main/java/com/cex/common/
│   │   ├── config/              # 公共配置
│   │   ├── exception/          # 异常处理
│   │   ├── response/           # 统一响应
│   │   ├── util/               # 工具类
│   │   ├── security/           # 安全相关
│   │   └── constant/           # 常量定义
│   └── pom.xml
├── gateway-service/             # 网关服务
│   ├── src/main/java/com/cex/gateway/
│   │   ├── config/             # 网关配置
│   │   ├── filter/            # 网关过滤器
│   │   └── GatewayApplication.java
│   └── pom.xml
├── user-service/               # 用户服务
│   ├── src/main/java/com/cex/user/
│   │   ├── controller/        # 控制器
│   │   ├── service/           # 业务逻辑
│   │   ├── mapper/            # 数据访问
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   └── UserApplication.java
│   └── pom.xml
├── trade-service/              # 交易服务
│   ├── src/main/java/com/cex/trade/
│   │   ├── controller/        # 交易相关
│   │   ├── service/           # 交易逻辑
│   │   ├── mapper/            # 交易数据
│   │   ├── entity/            # 交易实体
│   │   └── TradeApplication.java
│   └── pom.xml
├── wallet-service/             # 钱包服务
│   ├── src/main/java/com/cex/wallet/
│   │   ├── controller/        # 钱包相关
│   │   ├── service/           # 钱包逻辑
│   │   ├── mapper/            # 钱包数据
│   │   └── WalletApplication.java
│   └── pom.xml
├── market-service/             # 市场服务
│   ├── src/main/java/com/cex/market/
│   │   ├── controller/        # 市场数据
│   │   ├── service/           # 市场逻辑
│   │   ├── data/              # 数据抓取
│   │   └── MarketApplication.java
│   └── pom.xml
├── match-service/              # 撮合服务
│   ├── src/main/java/com/cex/match/
│   │   ├── engine/            # 撮合引擎
│   │   ├── orderbook/         # 订单簿
│   │   └── MatchApplication.java
│   └── pom.xml
├── finance-service/            # 财务服务
│   ├── src/main/java/com/cex/finance/
│   │   ├── controller/        # 财务相关
│   │   ├── service/           # 财务逻辑
│   │   └── FinanceApplication.java
│   └── pom.xml
├── risk-service/               # 风控服务
│   ├── src/main/java/com/cex/risk/
│   │   ├── controller/        # 风控相关
│   │   ├── engine/            # 风控引擎
│   │   └── RiskApplication.java
│   └── pom.xml
├── notify-service/             # 通知服务
│   ├── src/main/java/com/cex/notify/
│   │   ├── controller/        # 通知相关
│   │   ├── service/           # 通知逻辑
│   │   └── NotifyApplication.java
│   └── pom.xml
├── docker/                     # Docker 配置文件
│   ├── mysql/                  # MySQL 配置
│   ├── redis/                  # Redis 配置
│   ├── rocketmq/               # RocketMQ 配置
│   ├── nginx/                  # Nginx 配置
│   ├── prometheus/             # Prometheus 配置
│   └── grafana/                # Grafana 配置
├── docker-compose.yml          # 基础设施
├── docker-compose.dev.yml      # 开发环境
├── docker-compose.prod.yml     # 生产环境
└── README.md                   # 本文档
```

## 🎮 核心微服务

### 🚪 Gateway Service (网关服务)
- **端口**: 8080
- **功能**: API 网关、路由转发、负载均衡、限流熔断
- **技术**: Spring Cloud Gateway, Sentinel, JWT

### 👤 User Service (用户服务)
- **端口**: 8001
- **功能**: 用户注册登录、身份认证、权限管理、KYC认证
- **技术**: Spring Security, JWT, OAuth2

### 💹 Trade Service (交易服务)
- **端口**: 8002
- **功能**: 订单管理、交易执行、手续费计算
- **技术**: 事务管理、消息队列、分布式锁

### 💰 Wallet Service (钱包服务)
- **端口**: 8003
- **功能**: 资产管理、充值提现、地址生成、余额查询
- **技术**: 加密算法、区块链集成、事务安全

### 📊 Market Service (市场服务)
- **端口**: 8004
- **功能**: 行情数据、K线图表、市场统计
- **技术**: 数据抓取、WebSocket、实时计算

### ⚡ Match Service (撮合服务)
- **端口**: 8005
- **功能**: 订单撮合、价格发现、流动性管理
- **技术**: 撮合算法、内存计算、高性能处理

### 🏦 Finance Service (财务服务)
- **端口**: 8006
- **功能**: 财务对账、资金清算、风险控制
- **技术**: 财务计算、数据分析、报表生成

### 🛡️ Risk Service (风控服务)
- **端口**: 8007
- **功能**: 实时监控、异常检测、风险控制
- **技术**: 规则引擎、机器学习、实时计算

### 📧 Notify Service (通知服务)
- **端口**: 8008
- **功能**: 邮件通知、短信通知、站内消息
- **技术**: 消息队列、模板引擎、第三方集成

## 🔧 开发指南

### 代码规范

项目遵循以下规范：
- **Java 代码规范**: 遵循 Oracle Java 代码规范
- **Spring Boot 最佳实践**: 使用官方推荐配置
- **微服务设计原则**: 单一职责、服务自治

### 数据库设计

数据库表结构请参考：[数据库设计文档](../docs/02-数据库设计.md)

### API 接口

API 接口文档请参考：[API 接口设计](../docs/03-API接口设计.md)

### 消息队列

RocketMQ 消息设计请参考：[RocketMQ 设计文档](../docs/04-RocketMQ设计.md)

### 本地开发环境搭建

1. **克隆项目**
   ```bash
   git clone https://github.com/your-org/pp-cex.git
   cd pp-cex/backend
   ```

2. **启动依赖服务**
   ```bash
   docker-compose -f docker-compose.yml up -d
   ```

3. **配置环境变量**
   ```bash
   cp .env.example .env
   # 编辑 .env 文件配置数据库连接等
   ```

4. **编译并运行**
   ```bash
   mvn clean compile
   # 启动具体服务
   ```

### 单元测试

```bash
# 运行所有测试
mvn test

# 运行特定服务测试
cd user-service
mvn test
```

### 代码质量检查

```bash
# 代码格式化
mvn spotless:apply

# 代码检查
mvn checkstyle:check

# 依赖检查
mvn dependency:check
```

## 🚀 部署指南

### Docker 容器化部署

项目已完全容器化，支持 Docker Compose 一键部署：

```bash
# 开发环境
docker-compose -f docker-compose.dev.yml up -d

# 生产环境
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes 部署

项目支持 Kubernetes 部署，配置文件位于 `k8s/` 目录。

### 环境配置

不同环境的配置通过 Nacos 配置中心管理：

- **开发环境**: `cex-dev` 命名空间
- **测试环境**: `cex-test` 命名空间
- **生产环境**: `cex-prod` 命名空间

### 监控告警

- **应用监控**: Spring Boot Actuator + Prometheus
- **日志监控**: ELK Stack (Elasticsearch + Logstash + Kibana)
- **链路追踪**: SkyWalking
- **业务监控**: 自定义监控指标

## 🔗 相关链接

### 📚 项目文档
- [🏠 项目根目录 README](../README.md) - 项目概览和快速开始
- [🎨 前端项目文档](../frontend/README.md) - React技术栈和组件开发
- [📋 总体架构设计](../docs/01-总体架构设计.md) - 微服务架构详细说明
- [💾 数据库设计](../docs/02-数据库设计.md) - 数据库表结构和关系
- [🔌 API 接口设计](../docs/03-API接口设计.md) - RESTful API 规范
- [📨 RocketMQ 设计](../docs/04-RocketMQ设计.md) - 消息队列架构
- [🛡️ 安全设计](../docs/05-安全设计.md) - 安全架构和措施
- [🚀 部署设计](../docs/06-部署设计.md) - 完整部署方案

### 🛠️ 技术资源
- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Alibaba 文档](https://sca.aliyun.com/docs/2022/overview)
- [Nacos 官方文档](https://nacos.io/docs/latest/what-is-nacos)
- [RocketMQ 官方文档](https://rocketmq.apache.org/docs/)

## 📊 性能指标

### 系统性能目标

- **TPS**: 10,000+ (峰值)
- **响应时间**: < 100ms (P95)
- **并发用户**: 100,000+
- **可用性**: 99.9%

### 服务资源分配

- **用户服务**: 2C4G × 2实例
- **交易服务**: 4C8G × 2实例
- **钱包服务**: 2C4G × 2实例
- **撮合服务**: 8C16G × 2实例
- **其他服务**: 2C4G × 1实例

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源 - 查看 [LICENSE](../../LICENSE) 文件了解详情。

## 📞 联系我们

- 项目地址：[GitHub Repository](https://github.com/your-org/pp-cex)
- 问题反馈：[Issues](https://github.com/your-org/pp-cex/issues)
- 邮箱：dev@cex.com

---

⭐ 如果这个项目对您有帮助，请给个 Star 支持一下！