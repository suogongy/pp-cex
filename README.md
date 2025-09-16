# CEX - 加密货币交易平台

一个基于 Spring Cloud Alibaba 和 Next.js 的现代化中心化加密货币交易平台学习项目。

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
docker-compose -f docker-compose.dev.yml up -d

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f
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
├── frontend/                       # 前端应用
├── scripts/                        # 脚本文件
├── docker-compose.dev.yml          # 开发环境配置
├── docker-compose.prod.yml         # 生产环境配置
└── README.md                       # 项目说明
```

## 📚 文档

- [总体架构设计](./docs/01-总体架构设计.md)
- [数据库设计](./docs/02-数据库设计.md)
- [API接口设计](./docs/03-API接口设计.md)
- [RocketMQ设计](./docs/04-RocketMQ设计.md)
- [安全设计](./docs/05-安全设计.md)
- [部署设计](./docs/06-部署设计.md)

## ⚠️ 免责声明

本项目仅用于学习和研究目的，不用于生产环境。请勿用于实际交易，投资有风险，入市需谨慎。

---

🌟 如果这个项目对您有帮助，请给个 Star 支持一下！
