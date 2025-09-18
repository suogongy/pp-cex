-- 创建市场数据库
CREATE DATABASE IF NOT EXISTS market_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE market_db;

-- 交易对配置表 (从trade-service同步过来)
CREATE TABLE `market_pair` (
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

-- 行情数据表 (24小时统计)
CREATE TABLE `market_ticker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `symbol` varchar(20) NOT NULL COMMENT '交易对',
  `last_price` decimal(20,8) NOT NULL COMMENT '最新价格',
  `open_price` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '开盘价',
  `high_price` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '最高价',
  `low_price` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '最低价',
  `volume` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '成交量',
  `quote_volume` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '成交额',
  `price_change` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '价格变化',
  `price_change_percent` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '价格变化百分比',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT '成交次数',
  `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_symbol` (`symbol`),
  KEY `idx_last_update_time` (`last_update_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行情数据表';

-- K线数据表
CREATE TABLE `market_kline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `symbol` varchar(20) NOT NULL COMMENT '交易对',
  `interval` varchar(10) NOT NULL COMMENT '时间间隔 1m,5m,15m,30m,1h,4h,1d,1w,1M',
  `open_time` bigint(20) NOT NULL COMMENT '开盘时间戳',
  `close_time` bigint(20) NOT NULL COMMENT '收盘时间戳',
  `open_price` decimal(20,8) NOT NULL COMMENT '开盘价',
  `high_price` decimal(20,8) NOT NULL COMMENT '最高价',
  `low_price` decimal(20,8) NOT NULL COMMENT '最低价',
  `close_price` decimal(20,8) NOT NULL COMMENT '收盘价',
  `volume` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '成交量',
  `quote_volume` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '成交额',
  `trades_count` int(11) NOT NULL DEFAULT '0' COMMENT '成交次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_symbol_interval_open_time` (`symbol`, `interval`, `open_time`),
  KEY `idx_symbol_interval` (`symbol`, `interval`),
  KEY `idx_close_time` (`close_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='K线数据表';

-- 深度数据表 (订单簿快照)
CREATE TABLE `market_depth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `symbol` varchar(20) NOT NULL COMMENT '交易对',
  `bids` json NOT NULL COMMENT '买单 [[价格, 数量], ...]',
  `asks` json NOT NULL COMMENT '卖单 [[价格, 数量], ...]',
  `timestamp` bigint(20) NOT NULL COMMENT '时间戳',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_symbol` (`symbol`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='深度数据表';

-- 最新成交明细表
CREATE TABLE `market_trade` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `trade_id` varchar(32) NOT NULL COMMENT '成交ID',
  `symbol` varchar(20) NOT NULL COMMENT '交易对',
  `price` decimal(20,8) NOT NULL COMMENT '成交价格',
  `amount` decimal(20,8) NOT NULL COMMENT '成交数量',
  `quote_volume` decimal(20,8) NOT NULL COMMENT '成交额',
  `timestamp` bigint(20) NOT NULL COMMENT '成交时间戳',
  `is_buyer_maker` tinyint(1) NOT NULL COMMENT '是否买方挂单 1-是 0-否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_id` (`trade_id`),
  KEY `idx_symbol` (`symbol`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最新成交明细表';

-- 市场统计表
CREATE TABLE `market_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `symbol` varchar(20) NOT NULL COMMENT '交易对',
  `date` date NOT NULL COMMENT '统计日期',
  `open_price` decimal(20,8) NOT NULL COMMENT '开盘价',
  `close_price` decimal(20,8) NOT NULL COMMENT '收盘价',
  `high_price` decimal(20,8) NOT NULL COMMENT '最高价',
  `low_price` decimal(20,8) NOT NULL COMMENT '最低价',
  `volume` decimal(20,8) NOT NULL COMMENT '成交量',
  `quote_volume` decimal(20,8) NOT NULL COMMENT '成交额',
  `trade_count` int(11) NOT NULL COMMENT '成交次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_symbol_date` (`symbol`, `date`),
  KEY `idx_symbol` (`symbol`),
  KEY `idx_date` (`date`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场统计表';

-- 插入测试数据
INSERT INTO `market_pair` (`symbol`, `base_coin`, `quote_coin`, `pair_name`, `status`, `price_precision`, `amount_precision`, `min_amount`, `max_amount`, `min_price`, `max_price`, `fee_rate`, `sort_order`) VALUES
('BTCUSDT', 'BTC', 'USDT', 'Bitcoin/USDT', 1, 2, 6, 0.000100, 100.000000, 0.01, 1000000.00, 0.001000, 1),
('ETHUSDT', 'ETH', 'USDT', 'Ethereum/USDT', 1, 2, 5, 0.001000, 10000.000000, 0.01, 10000.00, 0.001000, 2),
('BNBUSDT', 'BNB', 'USDT', 'Binance Coin/USDT', 1, 2, 5, 0.010000, 10000.000000, 0.01, 10000.00, 0.001000, 3),
('ADAUSDT', 'ADA', 'USDT', 'Cardano/USDT', 1, 4, 2, 10.000000, 1000000.000000, 0.0001, 100.00, 0.001000, 4),
('SOLUSDT', 'SOL', 'USDT', 'Solana/USDT', 1, 2, 4, 0.010000, 100000.000000, 0.01, 10000.00, 0.001000, 5);

-- 插入初始行情数据
INSERT INTO `market_ticker` (`symbol`, `last_price`, `open_price`, `high_price`, `low_price`, `volume`, `quote_volume`, `price_change`, `price_change_percent`) VALUES
('BTCUSDT', 65000.00, 64000.00, 65500.00, 63800.00, 1000.500000, 65000000.00, 1000.00, 1.5625),
('ETHUSDT', 3200.00, 3150.00, 3250.00, 3100.00, 5000.000000, 16000000.00, 50.00, 1.5873),
('BNBUSDT', 580.00, 570.00, 590.00, 565.00, 10000.000000, 5800000.00, 10.00, 1.7544),
('ADAUSDT', 0.4500, 0.4400, 0.4600, 0.4300, 100000.000000, 45000.00, 0.0100, 2.2727),
('SOLUSDT', 150.00, 145.00, 155.00, 140.00, 20000.000000, 3000000.00, 5.00, 3.4483);