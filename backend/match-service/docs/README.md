# Match Service Nacos 配置文档

## 配置文件说明

本目录包含了 match-service 在 Nacos 配置中心所需的配置文件。这些配置文件需要手动上传到 Nacos 配置中心。

## Nacos 配置信息

### 基本信息
- **Namespace**: `d8a0e588-e615-448b-994c-0ad931c56808`
- **Group**: `match-service`

### 配置文件列表

| 配置文件名 | Data ID | 用途说明 |
|-----------|---------|----------|
| redis-config.yaml | redis-config.yaml | Redis 缓存配置 |
| rocketmq-config.yaml | rocketmq-config.yaml | RocketMQ 消息队列配置 |
| sentinel-config.yaml | sentinel-config.yaml | Sentinel 流量控制配置 |
| common-config.yaml | common-config.yaml | 通用配置和业务配置 |

## 配置上传步骤

### 1. 登录 Nacos 控制台
访问 Nacos 控制台：`http://localhost:8848/nacos`

### 2. 创建 Namespace
如果不存在以下 Namespace，请先创建：
- **命名空间ID**: `d8a0e588-e615-448b-994c-0ad931c56808`
- **命名空间名称**: `match-service-dev`

### 3. 上传配置文件

#### redis-config.yaml
- **Data ID**: `redis-config.yaml`
- **Group**: `match-service`
- **配置内容**: 复制 `redis-config.yaml` 文件内容

#### rocketmq-config.yaml
- **Data ID**: `rocketmq-config.yaml`
- **Group**: `match-service`
- **配置内容**: 复制 `rocketmq-config.yaml` 文件内容

#### sentinel-config.yaml
- **Data ID**: `sentinel-config.yaml`
- **Group**: `match-service`
- **配置内容**: 复制 `sentinel-config.yaml` 文件内容

#### common-config.yaml
- **Data ID**: `common-config.yaml`
- **Group**: `match-service`
- **配置内容**: 复制 `common-config.yaml` 文件内容

## 配置说明

### Redis 配置 (redis-config.yaml)
- Redis 连接信息
- 连接池配置
- 缓存策略配置

### RocketMQ 配置 (rocketmq-config.yaml)
- NameServer 地址
- Producer/Consumer 配置
- 消息主题和绑定配置

### Sentinel 配置 (sentinel-config.yaml)
- Sentinel Dashboard 地址
- 流控规则
- 降级规则
- 系统保护规则

### 通用配置 (common-config.yaml)
- 日志配置
- 监控配置
- 撮合引擎业务配置
- 缓存配置
- 线程池配置

## 配置验证

启动服务后，可以通过以下方式验证配置是否加载成功：

### 1. 检查服务注册
访问 Nacos 控制台，查看 `match-service` 是否成功注册。

### 2. 检查配置加载
查看服务日志，确认配置文件是否正确加载：
```bash
tail -f logs/match-service.log | grep "config"
```

### 3. 检查数据库连接
验证数据库是否可以正常连接：
```bash
curl -X GET "http://localhost:8005/match/health"
```

### 4. 检查消息队列
验证消息队列是否正常工作：
```bash
# 发送测试消息
curl -X POST "http://localhost:8005/match/test/message" \
  -H "Content-Type: application/json" \
  -d '{"test": true}'
```

## 配置更新

### 在线更新
Nacos 配置支持动态更新，修改配置后会自动生效。

### 监听配置变化
在应用日志中可以看到配置更新的信息：
```bash
tail -f logs/match-service.log | grep "refresh"
```

## 环境差异

### 开发环境 (dev)
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- RocketMQ: `localhost:9876`
- Nacos: `localhost:8848`

### 生产环境 (prod)
- MySQL: `mysql:3306`
- Redis: `redis:6379`
- RocketMQ: `rocketmq:9876`
- Nacos: `nacos:8848`

## 故障排查

### 配置加载失败
1. 检查 Nacos 服务是否正常运行
2. 验证 Namespace 和 Group 是否正确
3. 确认网络连接是否正常
4. 检查配置文件格式是否正确

### 缓存连接失败
1. 验证 Redis 配置是否正确
2. 检查 Redis 服务是否正常运行
3. 确认网络连接是否正常

### 消息队列连接失败
1. 验证 RocketMQ 配置是否正确
2. 检查 NameServer 是否正常运行
3. 确认主题和消费组配置是否正确

### Sentinel 连接失败
1. 验证 Sentinel Dashboard 是否正常运行
2. 检查配置规则是否正确
3. 确认网络连接是否正常

## 安全注意事项

1. **敏感信息**: 生产环境中不要在配置中直接包含密码等敏感信息
2. **访问控制**: 配置 Nacos 的访问权限控制
3. **配置加密**: 对敏感配置进行加密处理
4. **环境隔离**: 不同环境使用不同的 Namespace

## 配置备份

建议定期备份 Nacos 配置，可以通过以下方式：
1. Nacos 控制台手动导出
2. 使用 Nacos API 自动备份
3. 编写脚本定期备份配置文件