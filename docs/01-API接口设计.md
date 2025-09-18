# Web3 CEX API接口设计

## 1. API设计规范

### 1.1 设计原则
- **RESTful风格**: 遵循REST API设计原则
- **统一响应格式**: 标准化的响应结构
- **版本管理**: 支持API版本控制
- **安全认证**: JWT + OAuth2双重认证
- **限流保护**: 接口调用频率限制

### 1.2 请求响应格式

#### 1.2.1 统一响应结构
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1640995200000,
  "trace_id": "trace_123456789"
}
```

#### 1.2.2 错误响应结构
```json
{
  "code": 400,
  "message": "参数错误",
  "error": "Invalid parameter: email",
  "timestamp": 1640995200000,
  "trace_id": "trace_123456789"
}
```

### 1.3 状态码规范
| 状态码 | 说明 | 描述 |
|---------|------|------|
| 200 | 成功 | 请求处理成功 |
| 400 | 参数错误 | 请求参数验证失败 |
| 401 | 未授权 | 认证失败或Token过期 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 429 | 请求超限 | 调用频率超过限制 |
| 500 | 服务器错误 | 服务器内部错误 |
| 503 | 服务不可用 | 服务暂时不可用 |

### 1.4 接口版本管理
- **URL版本**: `/api/v1/`, `/api/v2/`
- **Header版本**: `Accept: application/vnd.ceX.v1+json`
- **向后兼容**: 新版本保持向后兼容

## 2. 用户服务API

### 2.1 认证相关

#### 2.1.1 用户注册
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "phone": "string",
  "password": "string",
  "confirm_password": "string",
  "verification_code": "string",
  "invite_code": "string"
}

Response:
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "user_id": 123456,
    "user_no": "U20240101001",
    "email": "user@example.com",
    "status": 1
  }
}
```

#### 2.1.2 用户登录
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string",
  "google_code": "string",
  "captcha": "string"
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 3600,
    "user_info": {
      "user_id": 123456,
      "username": "user123",
      "email": "user@example.com",
      "kyc_status": 0
    }
  }
}
```

#### 2.1.3 刷新Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refresh_token": "string"
}

Response:
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 3600
  }
}
```

### 2.2 用户管理

#### 2.2.1 获取用户信息
```http
GET /api/v1/user/info
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "user_id": 123456,
    "user_no": "U20240101001",
    "username": "user123",
    "email": "user@example.com",
    "phone": "+8613800138000",
    "nickname": "用户昵称",
    "avatar": "https://example.com/avatar.jpg",
    "country": "中国",
    "language": "zh-CN",
    "timezone": "Asia/Shanghai",
    "status": 1,
    "kyc_status": 0,
    "google_auth_enabled": false,
    "register_time": "2024-01-01T00:00:00Z",
    "last_login_time": "2024-01-01T12:00:00Z"
  }
}
```

#### 2.2.2 更新用户信息
```http
PUT /api/v1/user/info
Authorization: Bearer {token}
Content-Type: application/json

{
  "nickname": "string",
  "avatar": "string",
  "country": "string",
  "language": "string",
  "timezone": "string"
}

Response:
{
  "code": 200,
  "message": "更新成功"
}
```

### 2.3 安全管理

#### 2.3.1 修改密码
```http
PUT /api/v1/user/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "old_password": "string",
  "new_password": "string",
  "confirm_password": "string",
  "verification_code": "string"
}

Response:
{
  "code": 200,
  "message": "密码修改成功"
}
```

#### 2.3.2 启用Google 2FA
```http
POST /api/v1/user/google-2fa/enable
Authorization: Bearer {token}
Content-Type: application/json

{
  "secret": "string",
  "code": "string"
}

Response:
{
  "code": 200,
  "message": "Google 2FA启用成功",
  "data": {
    "recovery_codes": ["code1", "code2", "code3"]
  }
}
```

### 2.4 KYC认证

#### 2.4.1 提交KYC认证
```http
POST /api/v1/user/kyc
Authorization: Bearer {token}
Content-Type: multipart/form-data

{
  "real_name": "string",
  "id_card_type": "string",
  "id_card_no": "string",
  "nationality": "string",
  "address": "string",
  "birthday": "string",
  "gender": 1,
  "occupation": "string",
  "purpose": "string",
  "id_card_front": "file",
  "id_card_back": "file",
  "id_card_hand": "file"
}

Response:
{
  "code": 200,
  "message": "KYC认证提交成功，请等待审核"
}
```

#### 2.4.2 获取KYC状态
```http
GET /api/v1/user/kyc/status
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "status": 0,
    "real_name": "张三",
    "id_card_type": "身份证",
    "id_card_no": "110101199001011234",
    "nationality": "中国",
    "audit_time": null,
    "reject_reason": null
  }
}
```

## 3. 交易服务API

### 3.1 交易对管理

#### 3.1.1 获取交易对列表
```http
GET /api/v1/trade/pairs

Response:
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "symbol": "BTCUSDT",
      "base_coin": "BTC",
      "quote_coin": "USDT",
      "pair_name": "BTC/USDT",
      "status": 1,
      "price_precision": 2,
      "amount_precision": 6,
      "min_amount": "0.0001",
      "max_amount": "1000",
      "min_price": "0.01",
      "max_price": "1000000",
      "fee_rate": "0.001"
    }
  ]
}
```

#### 3.1.2 获取交易对详情
```http
GET /api/v1/trade/pairs/{symbol}

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "symbol": "BTCUSDT",
    "base_coin": "BTC",
    "quote_coin": "USDT",
    "pair_name": "BTC/USDT",
    "status": 1,
    "price_precision": 2,
    "amount_precision": 6,
    "min_amount": "0.0001",
    "max_amount": "1000",
    "min_price": "0.01",
    "max_price": "1000000",
    "fee_rate": "0.001",
    "last_price": "50000.00",
    "24h_change": "2.5",
    "24h_volume": "1000.5",
    "24h_high": "51000.00",
    "24h_low": "49000.00"
  }
}
```

### 3.2 订单管理

#### 3.2.1 创建订单
```http
POST /api/v1/trade/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "symbol": "BTCUSDT",
  "order_type": 1,
  "direction": 1,
  "price": "50000.00",
  "amount": "0.001",
  "time_in_force": 1
}

Response:
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "order_id": 123456,
    "order_no": "ORD20240101001",
    "symbol": "BTCUSDT",
    "order_type": 1,
    "direction": 1,
    "price": "50000.00",
    "amount": "0.001",
    "executed_amount": "0.00000000",
    "executed_value": "0.00000000",
    "status": 1,
    "create_time": "2024-01-01T00:00:00Z"
  }
}
```

#### 3.2.2 撤销订单
```http
DELETE /api/v1/trade/orders/{order_id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "订单撤销成功"
}
```

#### 3.2.3 获取订单列表
```http
GET /api/v1/trade/orders
Authorization: Bearer {token}
Query Parameters:
- symbol: 交易对 (可选)
- status: 订单状态 (可选)
- start_time: 开始时间 (可选)
- end_time: 结束时间 (可选)
- page: 页码 (默认1)
- size: 页大小 (默认20)

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 20,
    "orders": [
      {
        "order_id": 123456,
        "order_no": "ORD20240101001",
        "symbol": "BTCUSDT",
        "order_type": 1,
        "direction": 1,
        "price": "50000.00",
        "amount": "0.001",
        "executed_amount": "0.00050000",
        "executed_value": "25.00000000",
        "fee": "0.00100000",
        "status": 2,
        "create_time": "2024-01-01T00:00:00Z",
        "update_time": "2024-01-01T00:01:00Z"
      }
    ]
  }
}
```

## 4. 钱包服务API

### 4.1 资产管理

#### 4.1.1 获取资产列表
```http
GET /api/v1/wallet/assets
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "coin_id": "BTC",
      "coin_name": "Bitcoin",
      "available_balance": "1.00000000",
      "frozen_balance": "0.00000000",
      "total_balance": "1.00000000",
      "address": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
      "status": 1
    }
  ]
}
```

#### 4.1.2 获取充值地址
```http
GET /api/v1/wallet/deposit-address/{coin_id}
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "coin_id": "BTC",
    "coin_name": "Bitcoin",
    "address": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
    "qr_code": "data:image/png;base64,..."
  }
}
```

### 4.2 提现管理

#### 4.2.1 申请提现
```http
POST /api/v1/wallet/withdraw
Authorization: Bearer {token}
Content-Type: application/json

{
  "coin_id": "BTC",
  "amount": "0.001",
  "to_address": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
  "verification_code": "string"
}

Response:
{
  "code": 200,
  "message": "提现申请成功，请等待审核",
  "data": {
    "withdraw_id": 123456,
    "withdraw_no": "WD20240101001",
    "amount": "0.001",
    "fee": "0.0001",
    "actual_amount": "0.0009",
    "status": 1
  }
}
```

#### 4.2.2 获取提现记录
```http
GET /api/v1/wallet/withdrawals
Authorization: Bearer {token}
Query Parameters:
- coin_id: 币种 (可选)
- status: 状态 (可选)
- start_time: 开始时间 (可选)
- end_time: 结束时间 (可选)
- page: 页码 (默认1)
- size: 页大小 (默认20)

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 50,
    "page": 1,
    "size": 20,
    "withdrawals": [
      {
        "withdraw_id": 123456,
        "withdraw_no": "WD20240101001",
        "coin_id": "BTC",
        "coin_name": "Bitcoin",
        "amount": "0.001",
        "fee": "0.0001",
        "actual_amount": "0.0009",
        "to_address": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
        "status": 2,
        "tx_hash": "abc123...",
        "create_time": "2024-01-01T00:00:00Z",
        "complete_time": "2024-01-01T00:30:00Z"
      }
    ]
  }
}
```

## 5. 行情服务API

### 5.1 行情数据

#### 5.1.1 获取实时行情
```http
GET /api/v1/market/tickers

Response:
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "symbol": "BTCUSDT",
      "last_price": "50000.00",
      "bid_price": "49999.00",
      "ask_price": "50001.00",
      "24h_change": "2.5",
      "24h_volume": "1000.5",
      "24h_high": "51000.00",
      "24h_low": "49000.00",
      "timestamp": 1640995200000
    }
  ]
}
```

#### 5.1.2 获取K线数据
```http
GET /api/v1/market/klines
Query Parameters:
- symbol: 交易对 (必需)
- interval: 时间间隔 (1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w, 1M)
- limit: 数据条数 (默认100, 最大1000)
- start_time: 开始时间 (可选)
- end_time: 结束时间 (可选)

Response:
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "timestamp": 1640995200000,
      "open": "50000.00",
      "high": "51000.00",
      "low": "49000.00",
      "close": "50500.00",
      "volume": "100.5"
    }
  ]
}
```

### 5.2 深度数据

#### 5.2.1 获取深度数据
```http
GET /api/v1/market/depth/{symbol}
Query Parameters:
- limit: 深度条数 (默认20, 最大100)

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "symbol": "BTCUSDT",
    "timestamp": 1640995200000,
    "bids": [
      ["49999.00", "0.5"],
      ["49998.00", "1.2"]
    ],
    "asks": [
      ["50001.00", "0.3"],
      ["50002.00", "0.8"]
    ]
  }
}
```

#### 5.2.2 获取最新成交
```http
GET /api/v1/market/trades/{symbol}
Query Parameters:
- limit: 成交条数 (默认20, 最大100)

Response:
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "trade_id": 123456,
      "price": "50000.00",
      "amount": "0.001",
      "direction": "buy",
      "timestamp": 1640995200000
    }
  ]
}
```

## 6. 管理端API

### 6.1 用户管理

#### 6.1.1 获取用户列表
```http
GET /api/v1/admin/users
Authorization: Bearer {admin_token}
Query Parameters:
- username: 用户名 (可选)
- email: 邮箱 (可选)
- status: 状态 (可选)
- kyc_status: KYC状态 (可选)
- page: 页码 (默认1)
- size: 页大小 (默认20)

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 1000,
    "page": 1,
    "size": 20,
    "users": [
      {
        "user_id": 123456,
        "user_no": "U20240101001",
        "username": "user123",
        "email": "user@example.com",
        "status": 1,
        "kyc_status": 0,
        "register_time": "2024-01-01T00:00:00Z",
        "last_login_time": "2024-01-01T12:00:00Z"
      }
    ]
  }
}
```

#### 6.1.2 用户状态管理
```http
PUT /api/v1/admin/users/{user_id}/status
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "status": 2,
  "reason": "违反用户协议"
}

Response:
{
  "code": 200,
  "message": "用户状态更新成功"
}
```

### 6.2 财务管理

#### 6.2.1 获取财务报表
```http
GET /api/v1/admin/finance/reports
Authorization: Bearer {admin_token}
Query Parameters:
- start_date: 开始日期 (格式: YYYY-MM-DD)
- end_date: 结束日期 (格式: YYYY-MM-DD)
- report_type: 报表类型 (daily, weekly, monthly)

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "period": "2024-01-01 to 2024-01-31",
    "total_revenue": "100000.00",
    "total_fee": "1000.00",
    "total_users": 1000,
    "active_users": 800,
    "total_trades": 50000,
    "total_volume": "1000000.00"
  }
}
```

## 7. WebSocket API

### 7.1 实时行情推送

#### 7.1.1 连接地址
```
wss://api.example.com/ws/v1/market
```

#### 7.1.2 订阅行情
```json
{
  "method": "SUBSCRIBE",
  "params": ["ticker@BTCUSDT"],
  "id": 1
}
```

#### 7.1.3 取消订阅
```json
{
  "method": "UNSUBSCRIBE",
  "params": ["ticker@BTCUSDT"],
  "id": 2
}
```

#### 7.1.4 行情推送格式
```json
{
  "method": "ticker",
  "symbol": "BTCUSDT",
  "data": {
    "last_price": "50000.00",
    "bid_price": "49999.00",
    "ask_price": "50001.00",
    "24h_change": "2.5",
    "24h_volume": "1000.5",
    "timestamp": 1640995200000
  }
}
```

## 8. 错误码规范

### 8.1 通用错误码
| 错误码 | 错误信息 | 说明 |
|---------|----------|------|
| 10001 | 参数错误 | 请求参数格式错误或缺失 |
| 10002 | 认证失败 | Token无效或过期 |
| 10003 | 权限不足 | 用户没有操作权限 |
| 10004 | 频率限制 | 请求频率超过限制 |
| 10005 | 系统维护 | 系统正在维护中 |
| 10006 | 服务器错误 | 服务器内部错误 |

### 8.2 业务错误码
| 错误码 | 错误信息 | 说明 |
|---------|----------|------|
| 20001 | 用户不存在 | 用户不存在或已被删除 |
| 20002 | 密码错误 | 用户密码错误 |
| 20003 | 用户已存在 | 用户名或邮箱已存在 |
| 20004 | KYC未认证 | 用户未完成KYC认证 |
| 20005 | 账户已冻结 | 用户账户已被冻结 |
| 30001 | 交易对不存在 | 指定的交易对不存在 |
| 30002 | 订单不存在 | 指定的订单不存在 |
| 30003 | 订单状态错误 | 订单状态不允许操作 |
| 30004 | 余额不足 | 用户余额不足 |
| 30005 | 价格超出范围 | 订单价格超出允许范围 |
| 40001 | 币种不存在 | 指定的币种不存在 |
| 40002 | 提现金额不足 | 用户提现金额不足 |
| 40003 | 提现地址错误 | 提现地址格式错误 |
| 40004 | 充值确认中 | 充值正在确认中 |

## 9. 接口安全

### 9.1 认证机制
- **JWT Token**: 基于JWT的无状态认证
- **Token过期**: Access Token 1小时，Refresh Token 7天
- **密码策略**: 最小8位，包含大小写字母和数字
- **2FA认证**: 支持Google Authenticator

### 9.2 防护措施
- **限流保护**: 基于IP和用户的频率限制
- **参数验证**: 严格的参数格式验证
- **SQL注入防护**: 使用参数化查询
- **XSS防护**: 输入输出过滤
- **CSRF防护**: 使用CSRF Token

### 9.3 监控告警
- **接口监控**: 实时监控接口调用情况
- **错误监控**: 监控接口错误率
- **性能监控**: 监控接口响应时间
- **安全监控**: 监控异常请求行为

通过这套完整的API设计，系统提供了统一、安全、高效的接口服务，支持前端应用和第三方集成。