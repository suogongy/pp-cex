-- 创建交易数据库
CREATE DATABASE IF NOT EXISTS trade_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE trade_db;

-- 交易对配置表
CREATE TABLE `trade_pair` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '交易对ID',
  `symbol` varchar(20) NOT NULL COMMENT '交易对符号',
  `base_coin` varchar(32) NOT NULL COMMENT '基础币种',
  `quote_coin` varchar(32) NOT NULL COMMENT '计价币种',
  `pair_name` varchar(50) NOT NULL COMMENT '交易对名称',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-正常 2-暂停',
  `price_precision` int(11) NOT NULL DEFAULT '8' COMMENT '价格精度',
  `amount_precision` int(11) NOT NULL DEFAULT '8' COMMENT '数量精度',
  `min_amount` decimal(20,8) NOT NULL DEFAULT '0.00000001' COMMENT '最小数量',
  `max_amount` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大数量',
  `min_price` decimal(20,8) NOT NULL DEFAULT '0.00000001' COMMENT '最小价格',
  `max_price` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大价格',
  `fee_rate` decimal(10,6) NOT NULL DEFAULT '0.001000' COMMENT '手续费率',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_symbol` (`symbol`),
  KEY `idx_status` (`status`),
  KEY `idx_base_coin` (`base_coin`),
  KEY `idx_quote_coin` (`quote_coin`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易对配置表';

-- 订单表
CREATE TABLE `trade_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `symbol` varchar(20) NOT NULL COMMENT '交易对',
  `order_type` tinyint(1) NOT NULL COMMENT '订单类型 1-限价单 2-市价单',
  `direction` tinyint(1) NOT NULL COMMENT '买卖方向 1-买入 2-卖出',
  `price` decimal(20,8) NOT NULL COMMENT '价格',
  `amount` decimal(20,8) NOT NULL COMMENT '数量',
  `executed_amount` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '已成交数量',
  `executed_value` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '已成交金额',
  `fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '手续费',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-待成交 2-部分成交 3-完全成交 4-已取消',
  `time_in_force` tinyint(1) NOT NULL DEFAULT '1' COMMENT '时效类型 1-GTC 2-IOC 3-FOK',
  `source` tinyint(1) NOT NULL DEFAULT '1' COMMENT '来源 1-Web 2-API 3-App',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_symbol` (`symbol`),
  KEY `idx_status` (`status`),
  KEY `idx_order_type` (`order_type`),
  KEY `idx_direction` (`direction`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_price` (`price`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 成交明细表
CREATE TABLE `trade_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成交ID',
  `trade_no` varchar(32) NOT NULL COMMENT '成交编号',
  `symbol` varchar(20) NOT NULL COMMENT '交易对',
  `maker_order_id` bigint(20) NOT NULL COMMENT 'Maker订单ID',
  `taker_order_id` bigint(20) NOT NULL COMMENT 'Taker订单ID',
  `maker_user_id` bigint(20) NOT NULL COMMENT 'Maker用户ID',
  `taker_user_id` bigint(20) NOT NULL COMMENT 'Taker用户ID',
  `price` decimal(20,8) NOT NULL COMMENT '成交价格',
  `amount` decimal(20,8) NOT NULL COMMENT '成交数量',
  `value` decimal(20,8) NOT NULL COMMENT '成交金额',
  `maker_fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT 'Maker手续费',
  `taker_fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT 'Taker手续费',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成交时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_no` (`trade_no`),
  KEY `idx_symbol` (`symbol`),
  KEY `idx_maker_order_id` (`maker_order_id`),
  KEY `idx_taker_order_id` (`taker_order_id`),
  KEY `idx_maker_user_id` (`maker_user_id`),
  KEY `idx_taker_user_id` (`taker_user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成交明细表';

-- 插入测试数据
INSERT INTO `trade_pair` (`symbol`, `base_coin`, `quote_coin`, `pair_name`, `status`, `price_precision`, `amount_precision`, `min_amount`, `max_amount`, `min_price`, `max_price`, `fee_rate`, `sort_order`) VALUES
('BTCUSDT', 'BTC', 'USDT', 'Bitcoin/USDT', 1, 2, 6, 0.000100, 100.000000, 0.01, 1000000.00, 0.001000, 1),
('ETHUSDT', 'ETH', 'USDT', 'Ethereum/USDT', 1, 2, 5, 0.001000, 10000.000000, 0.01, 10000.00, 0.001000, 2),
('BNBUSDT', 'BNB', 'USDT', 'Binance Coin/USDT', 1, 2, 5, 0.010000, 10000.000000, 0.01, 10000.00, 0.001000, 3),
('ADAUSDT', 'ADA', 'USDT', 'Cardano/USDT', 1, 4, 2, 10.000000, 1000000.000000, 0.0001, 100.00, 0.001000, 4),
('SOLUSDT', 'SOL', 'USDT', 'Solana/USDT', 1, 2, 4, 0.010000, 100000.000000, 0.01, 10000.00, 0.001000, 5);