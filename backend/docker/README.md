# Docker Compose 基础设施配置

## 概述

此Docker Compose配置仅包含基础服务，不包含业务应用服务。适用于开发和生产环境的基础设施运行。

## 包含服务

- **MySQL**: 数据库服务
- **Redis**: 缓存服务
- **RocketMQ**: 消息队列（NameServer + Broker）
- **Nacos**: 配置中心和服务注册发现

## 使用方式

### 启动所有基础设施服务
```bash
# 进入backend目录
cd backend

# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d

# 查看服务状态
docker-compose -f docker/docker-compose.yml ps

# 停止所有服务
docker-compose -f docker/docker-compose.yml down
```

### 启动特定服务
```bash
# 仅启动数据库
docker-compose -f docker/docker-compose.yml up -d mysql redis

# 仅启动消息队列
docker-compose -f docker/docker-compose.yml up -d rocketmq-nameserver rocketmq-broker

# 仅启动配置中心
docker-compose -f docker/docker-compose.yml up -d nacos
```

### 管理命令
```bash
# 查看服务日志
docker-compose -f docker/docker-compose.yml logs -f [service_name]

# 重启服务
docker-compose -f docker/docker-compose.yml restart [service_name]

# 进入容器
docker-compose -f docker/docker-compose.yml exec [service_name] bash

# 查看实时资源使用
docker-compose -f docker/docker-compose.yml stats
```

## 端口配置

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库连接 |
| Redis | 6379 | 缓存连接 |
| RocketMQ NameServer | 9876 | 消息队列名称服务 |
| RocketMQ Broker | 10909,10911 | 消息队列代理服务 |
| Nacos | 8848 | 配置中心控制台 |

## 环境变量

可以通过环境变量或`.env`文件配置：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=root123
MYSQL_DATABASE=cex_db
MYSQL_USER=cex_user
MYSQL_PASSWORD=cex123
```

## 配置文件

配置文件位于 `./docker/` 目录下：
- `./docker/rocketmq/broker.conf` - RocketMQ Broker配置
- `./docker/rocketmq/broker-dev.conf` - 开发环境配置
- `./docker/rocketmq/broker-prod.conf` - 生产环境配置
- `./docker/mysql/` - MySQL配置文件
- `./docker/nginx/` - Nginx配置文件

## 数据持久化

所有数据通过Docker卷持久化：
- `mysql-data`: MySQL数据
- `redis-data`: Redis数据
- `rocketmq-*`: RocketMQ日志和存储
- `nacos-logs`: Nacos日志

## 数据库初始化

### Nacos数据库
Nacos使用MySQL存储配置数据，启动时会自动初始化数据库结构：
- MySQL启动时会创建基本的数据库结构
- Nacos首次启动时会自动创建`nacos`数据库和相关表
- 初始化脚本：`sql/nacos/nacos-schema.sql`

### 业务数据库
业务数据库脚本位于`sql/`目录：
- `finance_db.sql` - 财务系统数据库
- `wallet_db.sql` - 钱包系统数据库
- 其他业务数据库脚本