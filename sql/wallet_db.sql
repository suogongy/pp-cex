-- 钱包系统数据库设计
-- 创建钱包数据库
CREATE DATABASE IF NOT EXISTS wallet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE wallet_db;

-- 1. 钱包地址表
CREATE TABLE `wallet_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(50) NOT NULL COMMENT '币种名称',
  `address_type` tinyint(1) NOT NULL COMMENT '地址类型 1-热钱包 2-冷钱包',
  `address` varchar(255) NOT NULL COMMENT '钱包地址',
  `private_key` varchar(500) NOT NULL COMMENT '私钥(加密存储)',
  `public_key` varchar(255) DEFAULT NULL COMMENT '公钥',
  `mnemonic` varchar(500) DEFAULT NULL COMMENT '助记词(加密存储)',
  `wallet_type` varchar(50) NOT NULL COMMENT '钱包类型',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-正常 2-停用',
  `balance` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '余额',
  `min_balance` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '最小余额',
  `max_balance` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大余额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_address` (`address`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_address_type` (`address_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包地址表';

-- 2. 充值记录表
CREATE TABLE `recharge_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '充值ID',
  `recharge_no` varchar(32) NOT NULL COMMENT '充值编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(50) NOT NULL COMMENT '币种名称',
  `tx_hash` varchar(255) NOT NULL COMMENT '交易哈希',
  `from_address` varchar(255) NOT NULL COMMENT '来源地址',
  `to_address` varchar(255) NOT NULL COMMENT '目标地址',
  `amount` decimal(20,8) NOT NULL COMMENT '充值数量',
  `confirmations` int(11) NOT NULL DEFAULT '0' COMMENT '确认数',
  `required_confirmations` int(11) NOT NULL DEFAULT '6' COMMENT '需要确认数',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-待确认 2-已确认 3-已失败',
  `fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '手续费',
  `block_number` bigint(20) DEFAULT NULL COMMENT '区块号',
  `block_time` datetime DEFAULT NULL COMMENT '区块时间',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recharge_no` (`recharge_no`),
  UNIQUE KEY `uk_tx_hash` (`tx_hash`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_to_address` (`to_address`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- 3. 提现记录表
CREATE TABLE `withdraw_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提现ID',
  `withdraw_no` varchar(32) NOT NULL COMMENT '提现编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(50) NOT NULL COMMENT '币种名称',
  `to_address` varchar(255) NOT NULL COMMENT '目标地址',
  `amount` decimal(20,8) NOT NULL COMMENT '提现数量',
  `fee` decimal(20,8) NOT NULL COMMENT '手续费',
  `actual_amount` decimal(20,8) NOT NULL COMMENT '实际到账数量',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-待审核 2-已通过 3-已拒绝 4-处理中 5-已完成 6-已失败',
  `tx_hash` varchar(255) DEFAULT NULL COMMENT '交易哈希',
  `confirmations` int(11) DEFAULT '0' COMMENT '确认数',
  `required_confirmations` int(11) NOT NULL DEFAULT '6' COMMENT '需要确认数',
  `audit_user` varchar(32) DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `block_number` bigint(20) DEFAULT NULL COMMENT '区块号',
  `block_time` datetime DEFAULT NULL COMMENT '区块时间',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tx_hash` (`tx_hash`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';

-- 4. 钱包内部转账记录表
CREATE TABLE `wallet_transfer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '转账ID',
  `transfer_no` varchar(32) NOT NULL COMMENT '转账编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(50) NOT NULL COMMENT '币种名称',
  `from_wallet_type` varchar(50) NOT NULL COMMENT '来源钱包类型',
  `to_wallet_type` varchar(50) NOT NULL COMMENT '目标钱包类型',
  `amount` decimal(20,8) NOT NULL COMMENT '转账数量',
  `fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '手续费',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-待处理 2-已完成 3-已失败',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包内部转账记录表';

-- 5. 币种配置表
CREATE TABLE `coin_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(50) NOT NULL COMMENT '币种名称',
  `coin_symbol` varchar(20) NOT NULL COMMENT '币种符号',
  `chain_id` varchar(32) NOT NULL COMMENT '链ID',
  `chain_name` varchar(50) NOT NULL COMMENT '链名称',
  `contract_address` varchar(255) DEFAULT NULL COMMENT '合约地址',
  `decimals` int(11) NOT NULL DEFAULT '18' COMMENT '小数位数',
  `min_recharge_amount` decimal(20,8) NOT NULL DEFAULT '0.00000001' COMMENT '最小充值数量',
  `max_recharge_amount` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大充值数量',
  `min_withdraw_amount` decimal(20,8) NOT NULL DEFAULT '0.00000001' COMMENT '最小提现数量',
  `max_withdraw_amount` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大提现数量',
  `withdraw_fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '提现手续费',
  `withdraw_fee_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '手续费类型 1-固定 2-比例',
  `withdraw_fee_rate` decimal(10,6) NOT NULL DEFAULT '0.001000' COMMENT '提现手续费率',
  `required_confirmations` int(11) NOT NULL DEFAULT '6' COMMENT '需要确认数',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-启用 2-停用',
  `recharge_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '充值启用 1-启用 2-停用',
  `withdraw_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '提现启用 1-启用 2-停用',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coin_id` (`coin_id`),
  KEY `idx_chain_id` (`chain_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='币种配置表';

-- 6. 钱包监控日志表
CREATE TABLE `wallet_monitor_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `log_type` tinyint(1) NOT NULL COMMENT '日志类型 1-余额监控 2-交易监控 3-异常监控',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `address_type` tinyint(1) NOT NULL COMMENT '地址类型 1-热钱包 2-冷钱包',
  `address` varchar(255) NOT NULL COMMENT '钱包地址',
  `content` text NOT NULL COMMENT '日志内容',
  `level` tinyint(1) NOT NULL DEFAULT '1' COMMENT '级别 1-INFO 2-WARN 3-ERROR',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-已处理 2-待处理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_address_type` (`address_type`),
  KEY `idx_log_type` (`log_type`),
  KEY `idx_level` (`level`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包监控日志表';

-- 7. 钱包地址分配表
CREATE TABLE `wallet_address_allocation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分配ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `wallet_address_id` bigint(20) NOT NULL COMMENT '钱包地址ID',
  `address` varchar(255) NOT NULL COMMENT '分配地址',
  `allocate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-已分配 2-已回收',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_coin` (`user_id`, `coin_id`),
  KEY `idx_wallet_address_id` (`wallet_address_id`),
  KEY `idx_address` (`address`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包地址分配表';

-- 插入基础币种配置数据
INSERT INTO `coin_config` (`coin_id`, `coin_name`, `coin_symbol`, `chain_id`, `chain_name`, `decimals`, `min_recharge_amount`, `min_withdraw_amount`, `withdraw_fee`, `withdraw_fee_type`, `required_confirmations`, `sort_order`) VALUES
('BTC', 'Bitcoin', 'BTC', 'bitcoin', 'Bitcoin Mainnet', 8, '0.0001', '0.001', '0.0001', 1, 6, 1),
('ETH', 'Ethereum', 'ETH', 'ethereum', 'Ethereum Mainnet', 18, '0.01', '0.01', '0.005', 1, 12, 2),
('USDT', 'Tether', 'USDT', 'ethereum', 'Ethereum Mainnet', 6, '1.0', '1.0', '1.0', 1, 12, 3),
('USDT-TRON', 'Tether TRON', 'USDT', 'tron', 'TRON Mainnet', 6, '1.0', '1.0', '1.0', 1, 19, 4);

-- 创建索引
CREATE INDEX `idx_recharge_user_status` ON `recharge_record` (`user_id`, `status`);
CREATE INDEX `idx_withdraw_user_status` ON `withdraw_record` (`user_id`, `status`);
CREATE INDEX `idx_wallet_address_coin_type` ON `wallet_address` (`coin_id`, `address_type`, `status`);