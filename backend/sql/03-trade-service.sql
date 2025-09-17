-- ======================================================
-- 交易服务数据库初始化脚本
-- 创建时间：2025-09-17
-- 功能：包含交易相关所有表结构，适合微服务架构
-- 对应Java实体类：com.ppcex.trade.entity
-- ======================================================

-- 创建交易服务数据库
CREATE DATABASE IF NOT EXISTS `ppcex_trade` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `ppcex_trade`;

-- ======================================================
-- 交易对配置表 (trade_pair)
-- 对应实体类：com.ppcex.trade.entity.TradePair
-- ======================================================
CREATE TABLE IF NOT EXISTS `trade_pair` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `symbol` VARCHAR(20) NOT NULL COMMENT '交易对符号',
    `base_coin` VARCHAR(10) NOT NULL COMMENT '基础货币',
    `quote_coin` VARCHAR(10) NOT NULL COMMENT '计价货币',
    `pair_name` VARCHAR(50) NOT NULL COMMENT '交易对名称',
    `status` INT NOT NULL COMMENT '状态',
    `price_precision` INT NOT NULL COMMENT '价格精度',
    `amount_precision` INT NOT NULL COMMENT '数量精度',
    `min_amount` DECIMAL(20,8) NOT NULL COMMENT '最小数量',
    `max_amount` DECIMAL(20,8) NOT NULL COMMENT '最大数量',
    `min_price` DECIMAL(20,8) NOT NULL COMMENT '最小价格',
    `max_price` DECIMAL(20,8) NOT NULL COMMENT '最大价格',
    `fee_rate` DECIMAL(10,6) NOT NULL COMMENT '手续费率',
    `sort_order` INT NOT NULL COMMENT '排序序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_symbol` (`symbol`),
    KEY `idx_status` (`status`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易对配置表';

-- ======================================================
-- 订单表 (trade_order)
-- 对应实体类：com.ppcex.trade.entity.TradeOrder
-- ======================================================
CREATE TABLE IF NOT EXISTS `trade_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `symbol` VARCHAR(20) NOT NULL COMMENT '交易对符号',
    `order_type` INT NOT NULL COMMENT '订单类型',
    `direction` INT NOT NULL COMMENT '买卖方向',
    `price` DECIMAL(20,8) NOT NULL COMMENT '委托价格',
    `amount` DECIMAL(20,8) NOT NULL COMMENT '委托数量',
    `executed_amount` DECIMAL(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '已成交数量',
    `executed_value` DECIMAL(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '已成交金额',
    `fee` DECIMAL(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '手续费',
    `status` INT NOT NULL COMMENT '订单状态',
    `time_in_force` INT NOT NULL COMMENT '时效类型',
    `source` INT NOT NULL COMMENT '订单来源',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_symbol` (`symbol`),
    KEY `idx_status` (`status`),
    KEY `idx_order_type` (`order_type`),
    KEY `idx_direction` (`direction`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_expire_time` (`expire_time`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ======================================================
-- 交易明细表 (trade_detail)
-- 对应实体类：com.ppcex.trade.entity.TradeDetail
-- ======================================================
CREATE TABLE IF NOT EXISTS `trade_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trade_no` VARCHAR(32) NOT NULL COMMENT '成交编号',
    `symbol` VARCHAR(20) NOT NULL COMMENT '交易对符号',
    `maker_order_id` BIGINT NOT NULL COMMENT '挂单订单ID',
    `taker_order_id` BIGINT NOT NULL COMMENT '吃单订单ID',
    `maker_user_id` BIGINT NOT NULL COMMENT '挂单用户ID',
    `taker_user_id` BIGINT NOT NULL COMMENT '吃单用户ID',
    `price` DECIMAL(20,8) NOT NULL COMMENT '成交价格',
    `amount` DECIMAL(20,8) NOT NULL COMMENT '成交数量',
    `value` DECIMAL(20,8) NOT NULL COMMENT '成交金额',
    `maker_fee` DECIMAL(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '挂单手续费',
    `taker_fee` DECIMAL(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '吃单手续费',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_no` (`trade_no`),
    KEY `idx_symbol` (`symbol`),
    KEY `idx_maker_order_id` (`maker_order_id`),
    KEY `idx_taker_order_id` (`taker_order_id`),
    KEY `idx_maker_user_id` (`maker_user_id`),
    KEY `idx_taker_user_id` (`taker_user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_symbol_time` (`symbol`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易明细表';

-- ======================================================
-- 插入默认交易对数据
-- ======================================================
INSERT INTO `trade_pair` (`symbol`, `base_coin`, `quote_coin`, `pair_name`, `status`, `price_precision`, `amount_precision`, `min_amount`, `max_amount`, `min_price`, `max_price`, `fee_rate`, `sort_order`) VALUES
('BTCUSDT', 'BTC', 'USDT', 'Bitcoin/Tether', 1, 2, 8, 0.00000001, 10.00000000, 0.01, 1000000.00, 0.001000, 1),
('ETHUSDT', 'ETH', 'USDT', 'Ethereum/Tether', 1, 2, 8, 0.00000001, 1000.00000000, 0.01, 10000.00, 0.001000, 2),
('BNBUSDT', 'BNB', 'USDT', 'Binance Coin/Tether', 1, 2, 8, 0.00000001, 10000.00000000, 0.01, 5000.00, 0.001000, 3),
('ADAUSDT', 'ADA', 'USDT', 'Cardano/Tether', 1, 4, 6, 0.01, 1000000.00000000, 0.0001, 10.00, 0.001000, 4),
('SOLUSDT', 'SOL', 'USDT', 'Solana/Tether', 1, 2, 8, 0.00000001, 10000.00000000, 0.01, 1000.00, 0.001000, 5);

-- ======================================================
-- 创建视图
-- ======================================================
-- 用户订单汇总视图
CREATE VIEW `v_user_order_summary` AS
SELECT
    user_id,
    symbol,
    order_type,
    direction,
    status,
    COUNT(*) as order_count,
    SUM(amount) as total_amount,
    SUM(executed_amount) as total_executed_amount,
    SUM(executed_value) as total_executed_value,
    SUM(fee) as total_fee,
    MAX(create_time) as last_order_time,
    MIN(create_time) as first_order_time
FROM `trade_order`
GROUP BY user_id, symbol, order_type, direction, status;

-- 交易对统计视图
CREATE VIEW `v_pair_statistics` AS
SELECT
    p.symbol,
    p.pair_name,
    p.base_coin,
    p.quote_coin,
    p.status,
    p.fee_rate,
    COUNT(DISTINCT o.user_id) as active_traders,
    COUNT(o.id) as total_orders,
    SUM(o.executed_amount) as total_executed_amount,
    SUM(o.executed_value) as total_executed_value,
    SUM(o.fee) as total_fees,
    COUNT(d.id) as total_deals,
    MAX(d.create_time) as last_deal_time
FROM `trade_pair` p
LEFT JOIN `trade_order` o ON p.symbol = o.symbol
LEFT JOIN `trade_detail` d ON p.symbol = d.symbol
WHERE p.status = 1
GROUP BY p.symbol, p.pair_name, p.base_coin, p.quote_coin, p.status, p.fee_rate;

-- ======================================================
-- 数据库初始化完成
-- ======================================================

-- ======================================================
-- 交易服务数据库初始化完成
-- 创建时间：2025-09-17
-- 总表数：3个核心表
-- 总视图数：2个视图
-- 对应Java实体类：com.ppcex.trade.entity.TradePair, TradeOrder, TradeDetail
-- 专注于基本CRUD操作，业务逻辑在应用层处理
-- ======================================================