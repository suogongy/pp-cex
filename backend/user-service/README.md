# User Service - 用户服务

## 概述

用户服务是Web3 CEX系统的核心微服务之一，负责用户管理、认证授权、KYC认证等核心功能。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + MyBatis Plus
- **缓存**: Redis 7.x
- **消息队列**: RocketMQ 4.9.4
- **服务注册**: Nacos 2.2.x
- **文档**: Knife4j (Swagger)

## 功能特性

### 1. 用户管理
- 用户注册登录
- 用户信息管理
- 账户状态管理
- 登录日志记录

### 2. 认证授权
- JWT Token认证
- 密码安全策略
- 账户锁定机制
- 多因素认证支持

### 3. KYC认证
- 身份认证提交
- 证件审核管理
- 认证状态跟踪
- 重新提交功能

### 4. 安全管理
- 密码强度验证
- 登录失败限制
- 账户锁定机制
- 安全审计日志

## 数据库设计

### 核心表结构

1. **user_info** - 用户基本信息表
2. **user_kyc** - 用户KYC认证表
3. **user_asset** - 用户资产表
4. **user_login_log** - 用户登录日志表
5. **user_operation_log** - 用户操作日志表
6. **user_security_setting** - 用户安全设置表

### 详细设计请参考
- [`docs/user-service-design.md`](../docs/user-service-design.md)

## API接口

### 认证接口
- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 用户登录
- `POST /api/v1/auth/logout` - 用户登出
- `POST /api/v1/auth/refresh` - 刷新Token

### 用户信息接口
- `GET /api/v1/user/info` - 获取用户信息
- `PUT /api/v1/user/info` - 更新用户信息
- `PUT /api/v1/user/password` - 修改密码

### KYC认证接口
- `POST /api/v1/kyc/submit` - 提交KYC认证
- `POST /api/v1/kyc/resubmit` - 重新提交KYC认证
- `GET /api/v1/kyc/info` - 获取KYC信息
- `GET /api/v1/kyc/status` - 获取KYC状态

### 管理员接口
- `POST /api/v1/kyc/audit/{kycId}` - 审核KYC认证
- `GET /api/v1/kyc/pending-count` - 获取待审核KYC数量

## 安全特性

### JWT认证
- 使用HS512算法签名
- 支持访问令牌和刷新令牌
- Token过期时间可配置
- 支持用户信息携带

### 密码安全
- BCrypt密码加密
- 密码盐值保护
- 密码强度验证
- 密码修改审计

### 账户保护
- 登录失败次数限制
- 账户临时锁定机制
- 异常登录检测
- 安全日志记录

## 配置说明

### 环境配置
- `dev` - 开发环境
- `test` - 测试环境
- `prod` - 生产环境

### 主要配置项
```yaml
cex:
  user:
    jwt:
      secret: JWT密钥
      expiration: 24小时过期时间
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-digits: true
      require-special-chars: true
    login:
      max-failed-attempts: 5
      lock-duration: 30
```

## 部署说明

### 前置条件
- JDK 17+
- MySQL 8.0+
- Redis 7.x+
- Nacos 2.2.x+
- RocketMQ 4.9.x+

### 启动服务
```bash
# 编译项目
mvn clean package

# 启动服务
java -jar user-service-1.0.0.jar

# 或者使用Maven启动
mvn spring-boot:run
```

### 健康检查
```bash
# 服务健康检查
curl http://localhost:8001/user/actuator/health

# 服务信息
curl http://localhost:8001/user/actuator/info
```

## 监控指标

### 业务指标
- 用户注册量
- 用户登录成功率
- KYC认证通过率
- 账户锁定次数

### 技术指标
- API响应时间
- 数据库连接池状态
- 缓存命中率
- 系统资源使用情况

## 开发指南

### 代码结构
```
src/main/java/com/ppcex/user/
├── controller/          # 控制器层
├── service/            # 服务层
├── repository/         # 数据访问层
├── entity/             # 实体类
├── dto/               # 数据传输对象
├── config/            # 配置类
├── security/          # 安全相关
└── util/              # 工具类
```

### 开发规范
1. 遵循Spring Boot最佳实践
2. 使用统一的API响应格式
3. 完善的异常处理机制
4. 必要的日志记录
5. 代码注释和文档

### 测试
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn integration-test
```

## 问题排查

### 常见问题
1. **JWT Token过期**: 检查token过期时间配置
2. **数据库连接失败**: 检查数据库连接配置
3. **Redis连接失败**: 检查Redis服务状态
4. **服务注册失败**: 检查Nacos服务状态

### 日志分析
```bash
# 查看服务日志
tail -f logs/user-service.log

# 查看错误日志
grep ERROR logs/user-service.log
```

## 版本历史

### v1.0.0 (2025-09-17)
- 初始版本发布
- 实现用户注册登录功能
- 实现KYC认证功能
- 基础安全机制

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 发起 Pull Request

## 许可证

本项目采用 MIT 许可证。