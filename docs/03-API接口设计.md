# Web3 CEX API接口设计

## 1. 接口设计原则

### 1.1 设计目标
- **RESTful风格**: 遵循RESTful API设计规范
- **统一响应**: 统一的响应格式和状态码
- **安全性**: 接口认证、参数验证、访问控制
- **可扩展性**: 版本控制、易于扩展
- **可维护性**: 清晰的文档、规范的命名

### 1.2 设计规范
- **URL规范**: 小写字母，下划线分隔，复数名词
- **HTTP方法**: GET-查询，POST-创建，PUT-更新，DELETE-删除
- **响应格式**: JSON格式，统一结构
- **状态码**: 标准HTTP状态码
- **版本控制**: URL中包含版本号

## 2. 统一响应格式

### 2.1 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 具体数据
  },
  "timestamp": 1640995200000,
  "trace_id": "trace-123456789"
}
```

### 2.2 错误响应
```json
{
  "code": 400,
  "message": "参数错误",
  "error": "invalid_parameter",
  "data": {
    "field": "email",
    "message": "邮箱格式不正确"
  },
  "timestamp": 1640995200000,
  "trace_id": "trace-123456789"
}
```

### 2.3 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      // 数据列表
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100,
      "pages": 5
    }
  },
  "timestamp": 1640995200000,
  "trace_id": "trace-123456789"
}
```

## 3. 状态码定义

### 3.1 全局状态码
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 502 | 网关错误 |
| 503 | 服务不可用 |

### 3.2 业务状态码
| 状态码 | 说明 |
|--------|------|
| 1001 | 用户不存在 |
| 1002 | 密码错误 |
| 1003 | 用户已被禁用 |
| 1004 | 用户已存在 |
| 1005 | 验证码错误 |
| 2001 | 订单不存在 |
| 2002 | 订单状态错误 |
| 2003 | 余额不足 |
| 2004 | 超出交易限制 |
| 3001 | 钱包地址错误 |
| 3002 | 提现金额不足 |
| 3003 | 提现审核中 |
| 4001 | 风控规则触发 |

## 4. 用户服务API

### 4.1 用户注册
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

### 4.2 用户登录
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

### 4.3 获取用户信息
```http
GET /api/v1/user/info
Authorization: Bearer {token}
```

### 4.4 更新用户信息
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

### 4.5 修改密码
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

### 4.6 KYC认证
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
  "id_card_front": (file),
  "id_card_back": (file),
  "id_card_hand": (file)
}
```

## 5. 交易服务API

### 5.1 获取交易对列表
```http
GET /api/v1/trade/pairs
```

### 5.2 获取交易对详情
```http
GET /api/v1/trade/pairs/{symbol}
```

### 5.3 获取深度数据
```http
GET /api/v1/trade/depth/{symbol}?limit=100
```

### 5.4 获取最新成交
```http
GET /api/v1/trade/trades/{symbol}?limit=50
```

### 5.5 获取K线数据
```http
GET /api/v1/trade/klines/{symbol}?interval=1m&limit=100
```

### 5.6 创建订单
```http
POST /api/v1/trade/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "symbol": "BTCUSDT",
  "order_type": 1, // 1-限价单 2-市价单
  "direction": 1, // 1-买入 2-卖出
  "price": "50000.00",
  "amount": "0.001",
  "time_in_force": 1 // 1-GTC 2-IOC 3-FOK
}
```

### 5.7 查询订单
```http
GET /api/v1/trade/orders?symbol=BTCUSDT&status=1&page=1&size=20
Authorization: Bearer {token}
```

### 5.8 获取订单详情
```http
GET /api/v1/trade/orders/{order_id}
Authorization: Bearer {token}
```

### 5.9 取消订单
```http
DELETE /api/v1/trade/orders/{order_id}
Authorization: Bearer {token}
```

### 5.10 获取当前委托
```http
GET /api/v1/trade/open-orders?symbol=BTCUSDT
Authorization: Bearer {token}
```

### 5.11 获取历史订单
```http
GET /api/v1/trade/history-orders?symbol=BTCUSDT&page=1&size=20
Authorization: Bearer {token}
```

## 6. 钱包服务API

### 6.1 获取资产列表
```http
GET /api/v1/wallet/assets
Authorization: Bearer {token}
```

### 6.2 获取单个资产
```http
GET /api/v1/wallet/assets/{coin_id}
Authorization: Bearer {token}
```

### 6.3 获取充值地址
```http
GET /api/v1/wallet/deposit-address/{coin_id}
Authorization: Bearer {token}
```

### 6.4 获取充值记录
```http
GET /api/v1/wallet/deposit-history?coin_id=BTC&page=1&size=20
Authorization: Bearer {token}
```

### 6.5 创建提现
```http
POST /api/v1/wallet/withdraw
Authorization: Bearer {token}
Content-Type: application/json

{
  "coin_id": "BTC",
  "amount": "0.001",
  "address": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
  "memo": "提现到个人钱包",
  "verification_code": "123456",
  "google_code": "123456"
}
```

### 6.6 获取提现记录
```http
GET /api/v1/wallet/withdraw-history?coin_id=BTC&page=1&size=20
Authorization: Bearer {token}
```

### 6.7 获取资金流水
```http
GET /api/v1/wallet/financial-flow?coin_id=BTC&page=1&size=20
Authorization: Bearer {token}
```

## 7. 行情服务API

### 7.1 获取所有交易对行情
```http
GET /api/v1/market/tickers
```

### 7.2 获取单个交易对行情
```http
GET /api/v1/market/ticker/{symbol}
```

### 7.3 获取24小时行情统计
```http
GET /api/v1/market/24h-ticker/{symbol}
```

### 7.4 获取深度数据
```http
GET /api/v1/market/depth/{symbol}?limit=100
```

### 7.5 获取最新成交
```http
GET /api/v1/market/trades/{symbol}?limit=50
```

### 7.6 获取K线数据
```http
GET /api/v1/market/klines/{symbol}?interval=1m&limit=100
```

## 8. 风控服务API

### 8.1 获取用户风控状态
```http
GET /api/v1/risk/user-status
Authorization: Bearer {token}
```

### 8.2 获取交易限制
```http
GET /api/v1/risk/trading-limits
Authorization: Bearer {token}
```

### 8.3 获取IP白名单
```http
GET /api/v1/risk/ip-whitelist
Authorization: Bearer {token}
```

### 8.4 添加IP白名单
```http
POST /api/v1/risk/ip-whitelist
Authorization: Bearer {token}
Content-Type: application/json

{
  "ip": "192.168.1.100",
  "remark": "办公室IP"
}
```

## 9. WebSocket API

### 9.1 行情数据推送
```javascript
// 连接
const ws = new WebSocket('wss://api.example.com/ws/market');

// 订阅深度数据
ws.send(JSON.stringify({
  "method": "SUBSCRIBE",
  "params": ["depth.BTCUSDT"],
  "id": 1
}));

// 取消订阅
ws.send(JSON.stringify({
  "method": "UNSUBSCRIBE",
  "params": ["depth.BTCUSDT"],
  "id": 2
}));
```

### 9.2 用户数据推送
```javascript
// 连接
const ws = new WebSocket('wss://api.example.com/ws/user');

// 认证
ws.send(JSON.stringify({
  "method": "AUTH",
  "params": ["your_token"],
  "id": 1
}));

// 订阅订单更新
ws.send(JSON.stringify({
  "method": "SUBSCRIBE",
  "params": ["orders"],
  "id": 2
}));

// 订阅资产更新
ws.send(JSON.stringify({
  "method": "SUBSCRIBE",
  "params": ["assets"],
  "id": 3
}));
```

## 10. 认证与安全

### 10.1 JWT认证
```http
Authorization: Bearer {token}
```

### 10.2 API密钥认证
```http
X-API-Key: your_api_key
X-API-Secret: your_api_secret
X-API-Timestamp: 1640995200000
X-API-Signature: signature
```

### 10.3 请求签名
```javascript
const crypto = require('crypto');

function generateSignature(apiSecret, timestamp, method, path, body) {
  const message = `${timestamp}${method}${path}${body}`;
  return crypto.createHmac('sha256', apiSecret)
    .update(message)
    .digest('hex');
}
```

## 11. 错误处理

### 11.1 参数验证错误
```json
{
  "code": 400,
  "message": "参数验证失败",
  "error": "validation_failed",
  "data": {
    "email": "邮箱格式不正确",
    "password": "密码长度至少8位"
  }
}
```

### 11.2 业务逻辑错误
```json
{
  "code": 2003,
  "message": "余额不足",
  "error": "insufficient_balance",
  "data": {
    "available_balance": "0.00000000",
    "required_amount": "0.00100000"
  }
}
```

### 11.3 系统错误
```json
{
  "code": 500,
  "message": "服务器内部错误",
  "error": "internal_server_error",
  "data": null
}
```

## 12. 接口限流

### 12.1 限流规则
- **普通用户**: 1000次/分钟
- **VIP用户**: 5000次/分钟
- **机构用户**: 10000次/分钟

### 12.2 限流响应
```json
{
  "code": 429,
  "message": "请求过于频繁",
  "error": "rate_limit_exceeded",
  "data": {
    "limit": 1000,
    "remaining": 0,
    "reset": 1640995260000
  }
}
```

## 13. 版本控制

### 13.1 URL版本控制
```
/api/v1/user/info
/api/v2/user/info
```

### 13.2 版本兼容性
- **v1版本**: 继续支持1年
- **v2版本**: 推荐使用
- **废弃版本**: 提前3个月通知