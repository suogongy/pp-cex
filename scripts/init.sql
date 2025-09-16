-- CEX Database Initialization Script
-- Create database and user
CREATE DATABASE IF NOT EXISTS cex_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user if not exists
CREATE USER IF NOT EXISTS 'cex_user'@'%' IDENTIFIED BY 'cex_password';
GRANT ALL PRIVILEGES ON cex_db.* TO 'cex_user'@'%';
GRANT ALL PRIVILEGES ON nacos.* TO 'cex_user'@'%';
FLUSH PRIVILEGES;

-- Use cex_db
USE cex_db;

-- Create tables
-- User related tables
CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    salt VARCHAR(64) NOT NULL COMMENT '密码盐值',
    avatar VARCHAR(255) COMMENT '头像URL',
    nickname VARCHAR(50) COMMENT '昵称',
    level INT DEFAULT 1 COMMENT '用户等级',
    experience INT DEFAULT 0 COMMENT '经验值',
    invite_code VARCHAR(20) UNIQUE COMMENT '邀请码',
    referrer_id BIGINT COMMENT '邀请人ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-禁用 3-锁定',
    kyc_status TINYINT DEFAULT 0 COMMENT 'KYC状态 0-未认证 1-待审核 2-已认证 3-拒绝',
    email_verified BOOLEAN DEFAULT FALSE COMMENT '邮箱是否验证',
    phone_verified BOOLEAN DEFAULT FALSE COMMENT '手机是否验证',
    google_auth_enabled BOOLEAN DEFAULT FALSE COMMENT 'Google认证是否启用',
    google_auth_secret VARCHAR(32) COMMENT 'Google认证密钥',
    api_key VARCHAR(64) UNIQUE COMMENT 'API密钥',
    api_secret VARCHAR(128) COMMENT 'API密钥密钥',
    api_enabled BOOLEAN DEFAULT FALSE COMMENT 'API是否启用',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
    lock_time DATETIME COMMENT '锁定时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_invite_code (invite_code),
    INDEX idx_referrer_id (referrer_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

CREATE TABLE IF NOT EXISTS user_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    currency VARCHAR(20) NOT NULL COMMENT '币种',
    available DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '可用余额',
    frozen DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '冻结余额',
    total DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '总余额',
    btc_value DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT 'BTC价值',
    usd_value DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT 'USD价值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_currency (user_id, currency),
    INDEX idx_user_id (user_id),
    INDEX idx_currency (currency),
    INDEX idx_usd_value (usd_value),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资产表';

CREATE TABLE IF NOT EXISTS user_kyc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    id_type TINYINT NOT NULL COMMENT '证件类型 1-身份证 2-护照 3-驾驶证',
    id_number VARCHAR(50) NOT NULL COMMENT '证件号码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    nationality VARCHAR(50) COMMENT '国籍',
    birth_date DATE COMMENT '出生日期',
    address TEXT COMMENT '地址',
    front_image VARCHAR(255) COMMENT '正面照片',
    back_image VARCHAR(255) COMMENT '背面照片',
    selfie_image VARCHAR(255) COMMENT '自拍照片',
    status TINYINT DEFAULT 1 COMMENT '状态 1-待审核 2-已认证 3-拒绝',
    audit_time DATETIME COMMENT '审核时间',
    audit_user_id BIGINT COMMENT '审核人ID',
    audit_comment TEXT COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户KYC认证表';

CREATE TABLE IF NOT EXISTS user_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    login_type TINYINT NOT NULL COMMENT '登录类型 1-密码 2-验证码 3-Google 4-第三方',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    login_ip VARCHAR(45) NOT NULL COMMENT '登录IP',
    user_agent TEXT COMMENT '用户代理',
    device_info TEXT COMMENT '设备信息',
    location VARCHAR(100) COMMENT '地理位置',
    status TINYINT DEFAULT 1 COMMENT '状态 1-成功 2-失败',
    fail_reason VARCHAR(255) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    INDEX idx_login_ip (login_ip),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- Trading related tables
CREATE TABLE IF NOT EXISTS trade_pair (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE COMMENT '交易对符号',
    base_currency VARCHAR(20) NOT NULL COMMENT '基础货币',
    quote_currency VARCHAR(20) NOT NULL COMMENT '报价货币',
    price_precision INT DEFAULT 8 COMMENT '价格精度',
    amount_precision INT DEFAULT 8 COMMENT '数量精度',
    min_amount DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '最小交易量',
    max_amount DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '最大交易量',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-暂停 3-下线',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_symbol (symbol),
    INDEX idx_base_currency (base_currency),
    INDEX idx_quote_currency (quote_currency),
    INDEX idx_status (status),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易对表';

CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL UNIQUE COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    symbol VARCHAR(20) NOT NULL COMMENT '交易对',
    type TINYINT NOT NULL COMMENT '类型 1-买入 2-卖出',
    order_type TINYINT NOT NULL COMMENT '订单类型 1-限价 2-市价',
    price DECIMAL(36, 18) NOT NULL COMMENT '价格',
    amount DECIMAL(36, 18) NOT NULL COMMENT '数量',
    executed_amount DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '已成交数量',
    executed_value DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '已成交金额',
    fee DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '手续费',
    status TINYINT DEFAULT 1 COMMENT '状态 1-待成交 2-部分成交 3-完全成交 4-已撤销 5-过期',
    time_in_force TINYINT DEFAULT 1 COMMENT '时效性 1-GTC 2-IOC 3-FOK',
    source TINYINT DEFAULT 1 COMMENT '来源 1-Web 2-App 3-API',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_symbol (symbol),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易订单表';

CREATE TABLE IF NOT EXISTS trade_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_id VARCHAR(64) NOT NULL COMMENT '成交ID',
    order_id VARCHAR(64) NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    symbol VARCHAR(20) NOT NULL COMMENT '交易对',
    type TINYINT NOT NULL COMMENT '类型 1-买入 2-卖出',
    price DECIMAL(36, 18) NOT NULL COMMENT '成交价格',
    amount DECIMAL(36, 18) NOT NULL COMMENT '成交数量',
    fee DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '手续费',
    counter_order_id VARCHAR(64) COMMENT '对手方订单ID',
    counter_user_id BIGINT COMMENT '对手方用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trade_id (trade_id),
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_symbol (symbol),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成交明细表';

CREATE TABLE IF NOT EXISTS trade_fee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_level INT DEFAULT 1 COMMENT '用户等级',
    symbol VARCHAR(20) COMMENT '交易对',
    maker_fee DECIMAL(10, 8) NOT NULL DEFAULT 0.001 COMMENT '挂单费率',
    taker_fee DECIMAL(10, 8) NOT NULL DEFAULT 0.001 COMMENT '吃单费率',
    min_fee DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '最小手续费',
    max_fee DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '最大手续费',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_level (user_level),
    INDEX idx_symbol (symbol),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易费率表';

-- Wallet related tables
CREATE TABLE IF NOT EXISTS wallet_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    currency VARCHAR(20) NOT NULL COMMENT '币种',
    address VARCHAR(255) NOT NULL COMMENT '地址',
    memo VARCHAR(255) COMMENT '备注',
    network VARCHAR(50) NOT NULL COMMENT '网络',
    type TINYINT DEFAULT 1 COMMENT '类型 1-热钱包 2-冷钱包',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-禁用',
    balance DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '余额',
    private_key_encrypted TEXT COMMENT '加密私钥',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_currency (currency),
    INDEX idx_address (address),
    INDEX idx_type (type),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包地址表';

CREATE TABLE IF NOT EXISTS recharge_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tx_hash VARCHAR(128) NOT NULL UNIQUE COMMENT '交易哈希',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    currency VARCHAR(20) NOT NULL COMMENT '币种',
    amount DECIMAL(36, 18) NOT NULL COMMENT '金额',
    from_address VARCHAR(255) COMMENT '来源地址',
    to_address VARCHAR(255) NOT NULL COMMENT '目标地址',
    confirmations INT DEFAULT 0 COMMENT '确认数',
    required_confirmations INT DEFAULT 12 COMMENT '所需确认数',
    status TINYINT DEFAULT 1 COMMENT '状态 1-待确认 2-已确认 3-失败',
    block_number BIGINT COMMENT '区块号',
    block_time DATETIME COMMENT '区块时间',
    fee DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '手续费',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tx_hash (tx_hash),
    INDEX idx_user_id (user_id),
    INDEX idx_currency (currency),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

CREATE TABLE IF NOT EXISTS withdraw_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tx_hash VARCHAR(128) COMMENT '交易哈希',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    currency VARCHAR(20) NOT NULL COMMENT '币种',
    amount DECIMAL(36, 18) NOT NULL COMMENT '金额',
    fee DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '手续费',
    to_address VARCHAR(255) NOT NULL COMMENT '目标地址',
    network VARCHAR(50) NOT NULL COMMENT '网络',
    memo VARCHAR(255) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态 1-待审核 2-处理中 3-已完成 4-失败',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态 1-待审核 2-已通过 3-已拒绝',
    audit_user_id BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    audit_comment TEXT COMMENT '审核意见',
    block_number BIGINT COMMENT '区块号',
    block_time DATETIME COMMENT '区块时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tx_hash (tx_hash),
    INDEX idx_user_id (user_id),
    INDEX idx_currency (currency),
    INDEX idx_status (status),
    INDEX idx_audit_status (audit_status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现记录表';

CREATE TABLE IF NOT EXISTS wallet_transfer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_id VARCHAR(64) NOT NULL UNIQUE COMMENT '转账ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    currency VARCHAR(20) NOT NULL COMMENT '币种',
    amount DECIMAL(36, 18) NOT NULL COMMENT '金额',
    fee DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '手续费',
    from_address VARCHAR(255) NOT NULL COMMENT '来源地址',
    to_address VARCHAR(255) NOT NULL COMMENT '目标地址',
    type TINYINT DEFAULT 1 COMMENT '类型 1-内部转账 2-冷热转账',
    status TINYINT DEFAULT 1 COMMENT '状态 1-处理中 2-已完成 3-失败',
    tx_hash VARCHAR(128) COMMENT '交易哈希',
    block_number BIGINT COMMENT '区块号',
    block_time DATETIME COMMENT '区块时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_transfer_id (transfer_id),
    INDEX idx_user_id (user_id),
    INDEX idx_currency (currency),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包转账表';

-- Finance related tables
CREATE TABLE IF NOT EXISTS financial_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_id VARCHAR(64) NOT NULL UNIQUE COMMENT '流水ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type TINYINT NOT NULL COMMENT '类型 1-充值 2-提现 3-交易 4-手续费 5-转账 6-奖励',
    currency VARCHAR(20) NOT NULL COMMENT '币种',
    amount DECIMAL(36, 18) NOT NULL COMMENT '金额',
    balance DECIMAL(36, 18) NOT NULL COMMENT '余额',
    description VARCHAR(500) COMMENT '描述',
    reference_id VARCHAR(64) COMMENT '关联ID',
    reference_type VARCHAR(32) COMMENT '关联类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_flow_id (flow_id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_currency (currency),
    INDEX idx_reference_id (reference_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资金流水表';

CREATE TABLE IF NOT EXISTS financial_daily_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_date DATE NOT NULL COMMENT '报表日期',
    total_users INT NOT NULL DEFAULT 0 COMMENT '总用户数',
    new_users INT NOT NULL DEFAULT 0 COMMENT '新增用户数',
    active_users INT NOT NULL DEFAULT 0 COMMENT '活跃用户数',
    total_recharge DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '总充值金额',
    total_withdraw DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '总提现金额',
    total_trade_volume DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '总交易量',
    total_trade_fees DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '总交易手续费',
    total_withdraw_fees DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '总提现手续费',
    total_assets DECIMAL(36, 18) NOT NULL DEFAULT 0 COMMENT '总资产',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_report_date (report_date),
    INDEX idx_report_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财务日报表';

CREATE TABLE IF NOT EXISTS risk_control (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type TINYINT NOT NULL COMMENT '规则类型 1-用户 2-交易 3-资金 4-IP',
    condition JSON NOT NULL COMMENT '条件',
    action TINYINT NOT NULL COMMENT '动作 1-警告 2-限制 3-禁止',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 2-禁用',
    priority INT DEFAULT 0 COMMENT '优先级',
    description TEXT COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_type (rule_type),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控规则表';

CREATE TABLE IF NOT EXISTS risk_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    risk_level TINYINT NOT NULL COMMENT '风险等级 1-低 2-中 3-高',
    risk_score INT NOT NULL DEFAULT 0 COMMENT '风险分数',
    action TINYINT NOT NULL COMMENT '动作',
    detail JSON COMMENT '详情',
    ip VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_risk_level (risk_level),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES risk_control(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控日志表';

-- Insert default data
-- Insert default trade pairs
INSERT INTO trade_pair (symbol, base_currency, quote_currency, price_precision, amount_precision, min_amount, max_amount, status, sort) VALUES
('BTCUSDT', 'BTC', 'USDT', 2, 6, 0.0001, 100, 1, 1),
('ETHUSDT', 'ETH', 'USDT', 2, 6, 0.001, 1000, 1, 2),
('BNBUSDT', 'BNB', 'USDT', 4, 6, 0.01, 10000, 1, 3),
('ADAUSDT', 'ADA', 'USDT', 6, 0, 10, 100000, 1, 4),
('DOTUSDT', 'DOT', 'USDT', 4, 6, 0.1, 10000, 1, 5),
('SOLUSDT', 'SOL', 'USDT', 4, 6, 0.01, 10000, 1, 6),
('MATICUSDT', 'MATIC', 'USDT', 6, 0, 100, 1000000, 1, 7),
('AVAXUSDT', 'AVAX', 'USDT', 4, 6, 0.01, 10000, 1, 8),
('LINKUSDT', 'LINK', 'USDT', 4, 6, 0.1, 10000, 1, 9),
('UNIUSDT', 'UNI', 'USDT', 4, 6, 0.1, 10000, 1, 10);

-- Insert default fee rates
INSERT INTO trade_fee (user_level, maker_fee, taker_fee, min_fee, max_fee, status) VALUES
(1, 0.001, 0.001, 1, 100, 1),
(2, 0.0009, 0.001, 1, 100, 1),
(3, 0.0008, 0.0009, 1, 100, 1),
(4, 0.0007, 0.0008, 1, 100, 1),
(5, 0.0006, 0.0007, 1, 100, 1);

-- Insert default risk control rules
INSERT INTO risk_control (rule_name, rule_type, condition, action, status, priority, description) VALUES
('高频登录', 1, '{"login_count": 5, "time_window": 300}', 1, 1, 1, '5分钟内登录超过5次'),
('大额提现', 3, '{"amount": 10000, "currency": "USDT"}', 2, 1, 2, '单次提现超过10000 USDT'),
('异常IP登录', 1, '{"ip_change": true, "time_window": 3600}', 1, 1, 3, '1小时内IP地址变更'),
('交易频率过高', 2, '{"trade_count": 100, "time_window": 3600}', 2, 1, 4, '1小时内交易超过100次'),
('余额异常', 3, '{"balance_change": 0.5, "time_window": 86400}', 1, 1, 5, '24小时内余额变动超过50%');

-- Insert admin user
INSERT INTO user_info (username, email, password_hash, salt, status, level, email_verified, phone_verified, google_auth_enabled, api_enabled) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVYITi', 'somesalt', 1, 10, true, true, false, false);

-- Insert admin asset
INSERT INTO user_asset (user_id, currency, available, frozen, total, btc_value, usd_value) VALUES
(1, 'USDT', 1000000, 0, 1000000, 0, 1000000),
(1, 'BTC', 10, 0, 10, 10, 430000);