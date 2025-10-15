# CEX - 加密货币交易平台

🎓 **一个基于 Spring Cloud Alibaba 和 Next.js 的现代化中心化加密货币交易平台学习项目（非生产环境可用）**

## 🚀 快速开始

### 环境要求

- Docker >= 20.10
- Docker Compose >= 2.0
- Node.js >= 18 (开发环境)
- Java 17 (开发环境)
- Maven 3.8+ (开发环境)

### 一键启动

```bash
# 开发环境
./scripts/deploy.sh dev up

# 生产环境
./scripts/deploy.sh prod up
```

### 手动启动

#### 开发环境

```bash
# 复制环境变量
cp .env.example .env

# 启动开发环境
docker-compose -f backend/docker-compose.dev.yml up -d

# 查看日志
docker-compose -f backend/docker-compose.dev.yml logs -f
```

### 服务访问

开发环境端口映射：

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Next.js 应用 |
| 网关 | 8080 | API 网关 |
| 用户服务 | 8001 | 用户管理服务 |
| 交易服务 | 8002 | 交易服务 |
| 钱包服务 | 8003 | 钱包服务 |
| 市场服务 | 8004 | 市场数据服务 |
| 撮合服务 | 8005 | 撮合引擎 |
| 财务服务 | 8006 | 财务服务 |
| 风控服务 | 8007 | 风控服务 |
| 通知服务 | 8008 | 通知服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 服务注册中心 |
| RocketMQ | 9876 | 消息队列 |

## 🔧 技术栈

### 后端技术栈
- Spring Boot 3.x + Spring Cloud Alibaba 2022.x
- Nacos 2.x (服务注册、配置中心)
- RocketMQ 4.9.x (消息队列)
- MySQL 8.0 (主从复制)
- Redis 7.x (集群)
- JWT + Spring Security (认证授权)

### 前端技术栈
- Next.js 15 + TypeScript
- Ant Design + Tailwind CSS
- Zustand + React Query
- Wagmi + viem + ethers.js (Web3集成)
- ECharts + recharts (图表)

## 📋 功能特性

- 🔐 用户认证：多因素认证 (2FA、邮箱、短信)
- 💰 资产管理：多币种钱包、充值提现
- 📊 实时交易：限价单、市价单、止损单
- 📈 市场数据：实时行情、K线图、深度图
- 🛡️ 安全防护：风控引擎、异常监控
- 📱 响应式设计：完美适配 PC、移动端

## 📁 项目结构

```
pp-cex/
├── docs/                           # 技术文档
├── backend/                        # 后端微服务
│   ├── docker/                     # Docker配置文件
│   ├── docker-compose.dev.yml      # 开发环境配置
│   ├── docker-compose.prod.yml     # 生产环境配置
│   ├── docker-compose.yml          # 基础环境配置
│   └── README.md                   # 📖 后端项目详细文档
├── frontend/                       # 前端应用
│   └── README.md                   # 📖 前端项目详细文档
├── scripts/                        # 脚本文件
├── sql/                            # 数据库脚本
└── README.md                       # 项目说明
```

## 📚 项目文档

### 📋 文档说明

#### 系统级文档
- **00-系统架构设计.md**: 系统整体架构设计，包括项目概述、架构原则、微服务拆分、技术栈选型、性能指标等（整合后的完整架构文档）
- **01-API接口设计.md**: API接口设计文档，包括接口规范、认证授权等
- **02-安全设计.md**: 系统安全设计文档，包括安全策略、防护措施等
- **产品需求文档.md**: 产品需求规格文档，包括功能需求、非功能需求等

#### 服务级文档
每个服务目录下都包含：
- **README.md**: 服务技术设计文档，包含详细的业务流程图和时序图
- **服务概述**: 服务定位、核心职责、服务指标
- **技术架构**: 整体架构、技术栈、依赖关系
- **数据模型**: 核心数据表设计、数据关系
- **业务流程**: 详细的业务流程图和时序图
- **接口设计**: 核心接口清单和详细设计
- **缓存设计**: 缓存策略、缓存键设计、更新策略
- **消息队列设计**: 消息Topic、消息类型、消息格式
- **监控设计**: 业务监控、技术监控、告警规则

#### 🎯 文档特色

- **标准化格式**: 所有文档采用统一的文档结构和格式
- **可视化设计**: 丰富的业务流程图和时序图（使用Mermaid）
- **详细交互**: 完整展示服务间交互和依赖关系
- **生命周期**: 每个服务都有完整的生命周期流程图
- **系统架构**: 清晰的系统交互图和架构图

### 📖 详细文档
- [系统架构设计](./docs/00-系统架构设计.md) - 整体架构和技术选型
- [API接口设计](./docs/01-API接口设计.md) - RESTful API规范和接口文档
- [安全设计方案](./docs/02-安全设计.md) - 安全架构和防护机制
- [整体业务流程](./docs/03-整体流程.md) - 核心业务流程和时序图
- [产品需求文档](./docs/产品需求文档.md) - 详细的功能需求和规格说明

### 🔧 微服务文档
- [用户服务](./docs/services/user-service/README.md) - 用户管理、认证授权
- [交易服务](./docs/services/trade-service/README.md) - 订单管理、交易处理
- [钱包服务](./docs/services/wallet-service/README.md) - 资产管理、充值提现
- [财务服务](./docs/services/finance-service/README.md) - 财务管理、风险控制
- [撮合服务](./docs/services/match-service/README.md) - 撮合引擎、订单匹配
- [市场服务](./docs/services/market-service/README.md) - 行情数据、市场信息
- [风控服务](./docs/services/risk-service/README.md) - 风险评估、异常监控
- [通知服务](./docs/services/notify-service/README.md) - 消息推送、通知管理
- [网关服务](./docs/services/gateway-service/README.md) - API网关、路由转发

### 🚀 快速入门
- [🔗 后端项目文档](./backend/README.md) - 微服务架构、技术栈、开发指南
- [🔗 前端项目文档](./frontend/README.md) - React技术栈、组件库、部署指南

### 📊 技术栈详情
- **后端技术**: Spring Boot 3.2 + Spring Cloud Alibaba + MySQL + Redis + RocketMQ
- **前端技术**: Next.js 15 + TypeScript + Ant Design + Tailwind CSS + Web3

## ⚠️ 重要免责声明

**这是一个学习和研究项目，绝对不可用于生产环境！**

- 📚 **学习目的**：本项目仅供技术学习和研究使用
- 🚫 **非生产环境**：不具备生产环境所需的安全性、稳定性及合规性
- ⚠️ **风险提示**：请勿用于任何实际交易或资金处理
- 💰 **资金安全**：不要使用真实资金进行任何操作
- 🔐 **安全考虑**：代码未经过严格的安全审计和渗透测试
- 📋 **合规要求**：不满足金融监管机构的合规要求

**投资有风险，入市需谨慎。本项目仅供技术学习交流使用。**

---

🌟 如果这个项目对您有帮助，请给个 Star 支持一下！
