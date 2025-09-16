# 用户系统服务端详细设计

## 1. 系统架构设计

### 1.1 整体架构
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              用户服务层                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 用户管理     │  │ 认证授权     │  │ KYC认证     │  │ 资产管理     │             │
│  │User Manager │ │Auth Service │ │KYC Service │ │Asset Manager│             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 安全管理     │  │ 风控管理     │  │ 消息通知     │  │ 日志审计     │             │
│  │Security Mgr │ │Risk Manager │ │Notify Service│ │Audit Logger │             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              数据访问层                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 用户数据     │  │ KYC数据     │  │ 资产数据     │  │ 日志数据     │             │
│  │User DAO     │ │KYC DAO      │ │Asset DAO    │ │Log DAO      │             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ 缓存访问     │  │ 消息生产     │  │ 风控接口     │  │ 外部服务     │             │
│  │Cache Access │ │MQ Producer  │ │Risk Client  │ │External API │             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              基础设施层                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │ MySQL数据库  │  │ Redis缓存    │  │ RocketMQ    │  │ Nacos配置   │             │
│  │   User DB   │ │   Cache     │ │  Message    │ │  Config     │             │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 技术架构
- **框架**: Spring Boot 3.x + Spring Cloud Alibaba
- **数据库**: MySQL 8.0 (读写分离)
- **缓存**: Redis 7.x (分布式缓存)
- **消息队列**: RocketMQ 4.9.x
- **服务注册**: Nacos 2.2.x
- **安全框架**: Spring Security + JWT
- **监控**: Micrometer + Prometheus
- **日志**: Logback + ELK

### 1.3 核心功能模块
1. **用户管理**: 用户注册、信息管理、状态控制
2. **认证授权**: JWT认证、权限控制、会话管理
3. **KYC认证**: 身份验证、证件审核、合规管理
4. **资产管理**: 资产查询、余额管理、流水记录
5. **安全管理**: 密码管理、2FA认证、风控检查
6. **风控管理**: 风险评估、限制控制、异常处理

## 2. 数据库设计

### 2.1 用户相关表结构

#### 2.1.1 用户基本信息表 (user_info)
```sql
CREATE TABLE `user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_no` varchar(32) NOT NULL COMMENT '用户编号',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `salt` varchar(32) NOT NULL COMMENT '密码盐值',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `language` varchar(10) DEFAULT 'zh-CN' COMMENT '语言',
  `timezone` varchar(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-正常 2-冻结 3-注销',
  `kyc_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'KYC状态 0-未认证 1-已认证',
  `register_time` datetime NOT NULL COMMENT '注册时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(45) DEFAULT NULL COMMENT '最后登录IP',
  `google_auth_secret` varchar(32) DEFAULT NULL COMMENT 'Google认证密钥',
  `google_auth_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Google认证启用',
  `login_failed_count` int(11) NOT NULL DEFAULT '0' COMMENT '登录失败次数',
  `account_locked_until` datetime DEFAULT NULL COMMENT '账户锁定到期时间',
  `invite_code` varchar(32) DEFAULT NULL COMMENT '邀请码',
  `inviter_id` bigint(20) DEFAULT NULL COMMENT '邀请人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_kyc_status` (`kyc_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_inviter_id` (`inviter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基本信息表';
```

#### 2.1.2 用户登录日志表 (user_login_log)
```sql
CREATE TABLE `user_login_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `login_type` tinyint(1) NOT NULL COMMENT '登录类型 1-密码 2-Google 3-SMS',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `login_ip` varchar(45) NOT NULL COMMENT '登录IP',
  `login_location` varchar(100) DEFAULT NULL COMMENT '登录地点',
  `device_info` varchar(500) DEFAULT NULL COMMENT '设备信息',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `login_result` tinyint(1) NOT NULL COMMENT '登录结果 1-成功 2-失败',
  `fail_reason` varchar(200) DEFAULT NULL COMMENT '失败原因',
  `session_id` varchar(100) DEFAULT NULL COMMENT '会话ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_login_ip` (`login_ip`),
  KEY `idx_login_result` (`login_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录日志表';
```

#### 2.1.3 用户KYC表 (user_kyc)
```sql
CREATE TABLE `user_kyc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'KYC ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `real_name` varchar(100) NOT NULL COMMENT '真实姓名',
  `id_card_type` varchar(20) NOT NULL COMMENT '证件类型',
  `id_card_no` varchar(50) NOT NULL COMMENT '证件号码',
  `id_card_front` varchar(255) NOT NULL COMMENT '身份证正面照片',
  `id_card_back` varchar(255) NOT NULL COMMENT '身份证背面照片',
  `id_card_hand` varchar(255) DEFAULT NULL COMMENT '手持身份证照片',
  `nationality` varchar(50) NOT NULL COMMENT '国籍',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别 1-男 2-女',
  `address` varchar(255) NOT NULL COMMENT '地址',
  `occupation` varchar(100) DEFAULT NULL COMMENT '职业',
  `purpose` varchar(200) DEFAULT NULL COMMENT '交易目的',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态 0-待审核 1-已通过 2-已拒绝',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_user` varchar(32) DEFAULT NULL COMMENT '审核人',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_id_card_no` (`id_card_no`),
  KEY `idx_audit_time` (`audit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户KYC表';
```

#### 2.1.4 用户资产表 (user_asset)
```sql
CREATE TABLE `user_asset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资产ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(50) NOT NULL COMMENT '币种名称',
  `available_balance` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '可用余额',
  `frozen_balance` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '冻结余额',
  `total_balance` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '总余额',
  `address` varchar(255) DEFAULT NULL COMMENT '充值地址',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-正常 2-冻结',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_coin` (`user_id`, `coin_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_user_asset_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产表';
```

#### 2.1.5 用户操作日志表 (user_operation_log)
```sql
CREATE TABLE `user_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(200) NOT NULL COMMENT '操作描述',
  `operation_data` text DEFAULT NULL COMMENT '操作数据',
  `operation_result` tinyint(1) NOT NULL COMMENT '操作结果 1-成功 2-失败',
  `operation_ip` varchar(45) DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作日志表';
```

#### 2.1.6 用户安全设置表 (user_security_setting)
```sql
CREATE TABLE `user_security_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `login_alert_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '登录提醒启用',
  `withdraw_alert_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '提现提醒启用',
  `password_change_alert` tinyint(1) NOT NULL DEFAULT '1' COMMENT '密码修改提醒',
  `api_trading_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'API交易启用',
  `whitelist_withdraw_only` tinyint(1) NOT NULL DEFAULT '0' COMMENT '仅白名单提现',
  `anti_phishing_code` varchar(20) DEFAULT NULL COMMENT '反钓鱼码',
  `last_password_change` datetime DEFAULT NULL COMMENT '最后密码修改时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户安全设置表';
```

### 2.2 索引设计
- **主键索引**: 所有表设置主键索引
- **唯一索引**: 业务唯一字段（用户名、邮箱、手机号等）
- **普通索引**: 查询条件字段（状态、时间等）
- **复合索引**: 多字段查询条件

## 3. 接口设计

### 3.1 用户管理接口

#### 3.1.1 用户注册
```http
POST /api/v1/user/register
Content-Type: application/json

{
  "username": "user123",
  "email": "user@example.com",
  "phone": "+8613800138000",
  "password": "Password123!",
  "confirm_password": "Password123!",
  "verification_code": "123456",
  "invite_code": "INVITE123"
}
```

#### 3.1.2 用户登录
```http
POST /api/v1/user/login
Content-Type: application/json

{
  "username": "user123",
  "password": "Password123!",
  "google_code": "123456",
  "captcha": "abc123"
}
```

#### 3.1.3 获取用户信息
```http
GET /api/v1/user/info
Authorization: Bearer {token}
```

#### 3.1.4 更新用户信息
```http
PUT /api/v1/user/info
Authorization: Bearer {token}
Content-Type: application/json

{
  "nickname": "昵称",
  "avatar": "https://example.com/avatar.jpg",
  "country": "中国",
  "language": "zh-CN"
}
```

### 3.2 安全管理接口

#### 3.2.1 修改密码
```http
PUT /api/v1/user/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "old_password": "Password123!",
  "new_password": "NewPassword123!",
  "confirm_password": "NewPassword123!",
  "verification_code": "123456"
}
```

#### 3.2.2 启用Google 2FA
```http
POST /api/v1/user/google-2fa/enable
Authorization: Bearer {token}
Content-Type: application/json

{
  "secret": "JBSWY3DPEHPK3PXP",
  "code": "123456"
}
```

#### 3.2.3 禁用Google 2FA
```http
POST /api/v1/user/google-2fa/disable
Authorization: Bearer {token}
Content-Type: application/json

{
  "code": "123456",
  "password": "Password123!"
}
```

### 3.3 KYC认证接口

#### 3.3.1 提交KYC认证
```http
POST /api/v1/user/kyc
Authorization: Bearer {token}
Content-Type: multipart/form-data

{
  "real_name": "张三",
  "id_card_type": "身份证",
  "id_card_no": "110101199001011234",
  "nationality": "中国",
  "address": "北京市朝阳区",
  "birthday": "1990-01-01",
  "gender": 1,
  "occupation": "工程师",
  "purpose": "投资交易",
  "id_card_front": (file),
  "id_card_back": (file),
  "id_card_hand": (file)
}
```

#### 3.3.2 获取KYC状态
```http
GET /api/v1/user/kyc/status
Authorization: Bearer {token}
```

### 3.4 资产管理接口

#### 3.4.1 获取资产列表
```http
GET /api/v1/user/assets
Authorization: Bearer {token}
```

#### 3.4.2 获取单个资产
```http
GET /api/v1/user/assets/{coin_id}
Authorization: Bearer {token}
```

## 4. 安全设计

### 4.1 认证授权
- **JWT认证**: 使用JWT进行用户认证
- **角色权限**: RBAC权限控制
- **会话管理**: 会话超时和续期
- **多因素认证**: Google 2FA + 短信验证

### 4.2 数据安全
- **密码加密**: BCrypt密码加密
- **数据加密**: 敏感数据AES加密
- **数据脱敏**: 显示数据脱敏
- **访问控制**: 数据访问权限控制

### 4.3 风控安全
- **登录限制**: 登录失败次数限制
- **IP限制**: 异常IP检测
- **行为分析**: 用户行为分析
- **实时监控**: 实时安全监控

## 5. 缓存设计

### 5.1 缓存策略
- **用户信息缓存**: 缓存用户基本信息，TTL 30分钟
- **会话缓存**: 缓存用户会话信息，TTL 24小时
- **权限缓存**: 缓存用户权限信息，TTL 1小时
- **风控缓存**: 缓存风控规则，TTL 10分钟

### 5.2 缓存键设计
```
user:info:{user_id}          - 用户信息
user:session:{session_id}    - 用户会话
user:permission:{user_id}     - 用户权限
user:asset:{user_id}         - 用户资产
user:kyc:{user_id}           - 用户KYC状态
risk:rule:{rule_type}        - 风控规则
```

## 6. 消息队列设计

### 6.1 消息Topic
- **user-topic**: 用户相关消息
- **auth-topic**: 认证相关消息
- **kyc-topic**: KYC相关消息
- **asset-topic**: 资产相关消息
- **risk-topic**: 风控相关消息

### 6.2 消息类型
- **用户注册消息**: 用户成功注册后发送
- **用户登录消息**: 用户登录后发送
- **KYC审核消息**: KYC状态变更后发送
- **资产变动消息**: 资产余额变动后发送
- **风控告警消息**: 风控事件触发后发送

## 7. 监控设计

### 7.1 业务监控
- **用户注册量**: 新用户注册数量
- **活跃用户数**: 日活跃用户数
- **登录成功率**: 用户登录成功率
- **KYC通过率**: KYC认证通过率
- **资产总额**: 用户资产总金额

### 7.2 技术监控
- **API响应时间**: 接口响应时间
- **数据库连接池**: 数据库连接池状态
- **缓存命中率**: 缓存命中率统计
- **消息队列积压**: 消息队列积压情况
- **错误率**: 系统错误率统计

## 8. 性能设计

### 8.1 性能目标
- **响应时间**: API响应时间 < 200ms
- **并发处理**: 支持1000+并发用户
- **可用性**: 99.9%系统可用性
- **数据一致性**: 强数据一致性保证

### 8.2 性能优化
- **数据库优化**: 索引优化、读写分离
- **缓存优化**: 多级缓存、缓存预热
- **连接池优化**: 合理配置连接池
- **异步处理**: 异步消息处理

## 9. 扩展性设计

### 9.1 水平扩展
- **服务扩展**: 支持多实例部署
- **数据库扩展**: 支持读写分离、分库分表
- **缓存扩展**: 支持Redis集群
- **消息队列扩展**: 支持Broker集群

### 9.2 功能扩展
- **插件化设计**: 功能模块插件化
- **配置化设计**: 业务规则配置化
- **多语言支持**: 支持多语言国际化
- **多币种支持**: 支持多币种资产管理

## 10. 部署设计

### 10.1 部署架构
- **容器化**: Docker容器部署
- **编排**: Kubernetes集群管理
- **负载均衡**: Nginx负载均衡
- **服务发现**: Nacos服务注册发现

### 10.2 配置管理
- **配置中心**: Nacos配置管理
- **环境隔离**: 开发、测试、生产环境隔离
- **敏感信息**: 敏感信息加密存储
- **配置热更新**: 支持配置热更新

## 11. 测试设计

### 11.1 单元测试
- **服务层测试**: 业务逻辑测试
- **数据层测试**: 数据访问测试
- **工具类测试**: 工具方法测试
- **异常测试**: 异常场景测试

### 11.2 集成测试
- **API测试**: 接口功能测试
- **数据库测试**: 数据库集成测试
- **缓存测试**: 缓存功能测试
- **消息测试**: 消息队列测试

### 11.3 性能测试
- **压力测试**: 高并发压力测试
- **负载测试**: 系统负载测试
- **稳定性测试**: 长时间稳定性测试
- **容量测试**: 系统容量测试

## 12. 运维设计

### 12.1 日志管理
- **日志收集**: ELK日志收集
- **日志分析**: 日志分析监控
- **日志归档**: 日志归档存储
- **日志安全**: 日志安全保护

### 12.2 监控告警
- **系统监控**: 系统资源监控
- **业务监控**: 业务指标监控
- **告警通知**: 异常告警通知
- **故障处理**: 故障快速处理

### 12.3 备份恢复
- **数据备份**: 定期数据备份
- **备份验证**: 备份数据验证
- **恢复测试**: 恢复流程测试
- **灾难恢复**: 灾难恢复方案