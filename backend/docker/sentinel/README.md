# Sentinel 配置说明

## 服务信息

- **镜像**: `bladex/sentinel-dashboard:1.8.6`
- **容器名**: `sentinel`
- **Web控制台**: http://localhost:8858
- **API端口**: 8719
- **日志目录**: `./docker/sentinel/logs`

## 端口配置

| 端口 | 用途 | 说明 |
|------|------|------|
| 8858 | Web控制台 | Sentinel Dashboard管理界面 |
| 8719 | Sentinel API | 客户端通信端口 |

## 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `JAVA_OPTS` | - | JVM启动参数 |
| `NACOS_HOST` | `nacos` | Nacos服务器地址 |
| `NACOS_PORT` | `8848` | Nacos服务器端口 |
| `NACOS_NAMESPACE` | `public` | Nacos命名空间 |
| `NACOS_GROUP` | `DEFAULT_GROUP` | Nacos分组 |

## 使用说明

### 1. 启动 Sentinel 服务

```bash
# 在 docker-compose.yml 所在目录执行
docker-compose up -d sentinel
```

### 2. 访问 Sentinel Dashboard

打开浏览器访问：http://localhost:8858

默认登录信息：
- 用户名：`sentinel`
- 密码：`sentinel`

### 3. 配置规则推送

Sentinel 支持从 Nacos 配置中心拉取规则配置：

1. **推送规则到 Nacos**
   - 登录 Nacos 控制台 (http://localhost:8848)
   - 在 `public` 命名空间下创建配置文件
   - Data ID 格式：`sentinel-[service]-flow.json`
   - Group：`DEFAULT_GROUP`

2. **Sentinel 自动拉取**
   - 微服务启动后会自动从 Nacos 拉取规则
   - 支持规则的热更新

### 4. 集成示例

微服务中的 Sentinel 配置示例：

```yaml
spring:
  cloud:
    sentinel:
      datasource:
        flow-rules:
          nacos:
            server-addr: localhost:8848
            dataId: sentinel-user-service-flow.json
            rule-type: flow
```

## 规则配置示例

### 流控规则 (flow.json)
```json
[
  {
    "resource": "GET:/api/v1/user/info",
    "grade": 1,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0
  }
]
```

### 降级规则 (degrade.json)
```json
[
  {
    "resource": "GET:/api/v1/user/info",
    "grade": 0,
    "count": 10,
    "timeWindow": 60,
    "minRequestAmount": 20
  }
]
```

## 注意事项

1. **端口冲突**: 确保 8858 和 8719 端口未被占用
2. **Nacos 依赖**: Sentinel 依赖 Nacos 服务，请确保 Nacos 已启动
3. **网络连通**: 确保 Sentinel 能正常访问 Nacos 服务
4. **规则同步**: 修改 Nacos 中的规则后，可能需要重启微服务才能生效

## 日志查看

```bash
# 查看 Sentinel 容器日志
docker logs -f sentinel

# 查看持久化日志
ls -la ./docker/sentinel/logs/
```

## 停止服务

```bash
docker-compose stop sentinel
```

## 重启服务

```bash
docker-compose restart sentinel
```