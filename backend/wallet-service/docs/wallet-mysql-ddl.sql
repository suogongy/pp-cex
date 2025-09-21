-- 创建钱包数据库
CREATE DATABASE IF NOT EXISTS ppcex_wallet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ppcex_wallet;

-- 钱包地址表
CREATE TABLE `wallet_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(64) NOT NULL COMMENT '币种名称',
  `address_type` int(11) NOT NULL DEFAULT '1' COMMENT '地址类型 1-热钱包 2-冷钱包',
  `address` varchar(255) NOT NULL COMMENT '钱包地址',
  `private_key` text DEFAULT NULL COMMENT '私钥',
  `public_key` text DEFAULT NULL COMMENT '公钥',
  `mnemonic` text DEFAULT NULL COMMENT '助记词',
  `wallet_type` varchar(32) NOT NULL COMMENT '钱包类型',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态 1-正常 2-禁用',
  `balance` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '余额',
  `min_balance` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '最小余额阈值',
  `max_balance` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大余额阈值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_address` (`address`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_address_type` (`address_type`),
  KEY `idx_status` (`status`),
  KEY `idx_balance` (`balance`),
  KEY `idx_wallet_type` (`wallet_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包地址表';

-- 充值记录表
CREATE TABLE `recharge_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '充值ID',
  `recharge_no` varchar(32) NOT NULL COMMENT '充值编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(64) NOT NULL COMMENT '币种名称',
  `tx_hash` varchar(255) DEFAULT NULL COMMENT '交易哈希',
  `from_address` varchar(255) DEFAULT NULL COMMENT '来源地址',
  `to_address` varchar(255) NOT NULL COMMENT '目标地址',
  `amount` decimal(20,8) NOT NULL COMMENT '充值数量',
  `confirmations` int(11) NOT NULL DEFAULT '0' COMMENT '确认数',
  `required_confirmations` int(11) NOT NULL DEFAULT '6' COMMENT '所需确认数',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态 1-待确认 2-已确认',
  `fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '手续费',
  `block_number` bigint(20) DEFAULT NULL COMMENT '区块高度',
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
  KEY `idx_create_time` (`create_time`),
  KEY `idx_confirmations` (`confirmations`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- 提现记录表
CREATE TABLE `withdraw_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提现ID',
  `withdraw_no` varchar(32) NOT NULL COMMENT '提现编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(64) NOT NULL COMMENT '币种名称',
  `to_address` varchar(255) NOT NULL COMMENT '目标地址',
  `amount` decimal(20,8) NOT NULL COMMENT '提现数量',
  `fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '手续费',
  `actual_amount` decimal(20,8) NOT NULL COMMENT '实际到账数量',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态 1-待审核 2-审核通过 3-处理中 4-已完成 5-已拒绝',
  `tx_hash` varchar(255) DEFAULT NULL COMMENT '交易哈希',
  `confirmations` int(11) NOT NULL DEFAULT '0' COMMENT '确认数',
  `required_confirmations` int(11) NOT NULL DEFAULT '6' COMMENT '所需确认数',
  `audit_user` varchar(64) DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `block_number` bigint(20) DEFAULT NULL COMMENT '区块高度',
  `block_time` datetime DEFAULT NULL COMMENT '区块时间',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  UNIQUE KEY `uk_tx_hash` (`tx_hash`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_audit_user` (`audit_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';

-- 币种配置表
CREATE TABLE `coin_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(64) NOT NULL COMMENT '币种名称',
  `coin_symbol` varchar(32) NOT NULL COMMENT '币种符号',
  `chain_id` varchar(32) NOT NULL COMMENT '链ID',
  `chain_name` varchar(64) NOT NULL COMMENT '链名称',
  `contract_address` varchar(255) DEFAULT NULL COMMENT '合约地址',
  `decimals` int(11) NOT NULL DEFAULT '18' COMMENT '精度',
  `min_recharge_amount` decimal(20,8) NOT NULL DEFAULT '0.00000001' COMMENT '最小充值金额',
  `max_recharge_amount` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大充值金额',
  `min_withdraw_amount` decimal(20,8) NOT NULL DEFAULT '0.00000001' COMMENT '最小提现金额',
  `max_withdraw_amount` decimal(20,8) NOT NULL DEFAULT '1000000.00000000' COMMENT '最大提现金额',
  `withdraw_fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '提现手续费',
  `withdraw_fee_type` int(11) NOT NULL DEFAULT '1' COMMENT '手续费类型 1-固定 2-比例',
  `withdraw_fee_rate` decimal(10,6) NOT NULL DEFAULT '0.001000' COMMENT '提现手续费率',
  `required_confirmations` int(11) NOT NULL DEFAULT '6' COMMENT '所需确认数',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态 1-启用 2-禁用',
  `recharge_enabled` int(11) NOT NULL DEFAULT '1' COMMENT '充值是否启用 1-启用 0-禁用',
  `withdraw_enabled` int(11) NOT NULL DEFAULT '1' COMMENT '提现是否启用 1-启用 0-禁用',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coin_chain` (`coin_id`, `chain_id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_chain_id` (`chain_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='币种配置表';

-- 插入基础币种配置数据
INSERT INTO `coin_config` (`coin_id`, `coin_name`, `coin_symbol`, `chain_id`, `chain_name`, `contract_address`, `decimals`, `min_recharge_amount`, `max_recharge_amount`, `min_withdraw_amount`, `max_withdraw_amount`, `withdraw_fee`, `withdraw_fee_type`, `withdraw_fee_rate`, `required_confirmations`, `status`, `recharge_enabled`, `withdraw_enabled`, `sort_order`) VALUES
('BTC', 'Bitcoin', 'BTC', 'bitcoin', 'Bitcoin', NULL, 8, 0.00010000, 1000.00000000, 0.00050000, 100.00000000, 0.00010000, 1, 0.001000, 6, 1, 1, 1, 1),
('ETH', 'Ethereum', 'ETH', 'ethereum', 'Ethereum', NULL, 18, 0.00100000, 10000.00000000, 0.01000000, 5000.00000000, 0.00100000, 1, 0.001000, 12, 1, 1, 1, 2),
('USDT', 'Tether', 'USDT', 'ethereum', 'Ethereum', '0xdAC17F958D2ee523a2206206994597C13D831ec7', 6, 1.00000000, 1000000.00000000, 10.00000000, 500000.00000000, 1.00000000, 1, 0.001000, 12, 1, 1, 1, 3),
('USDT', 'Tether', 'USDT', 'tron', 'Tron', 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', 6, 1.00000000, 1000000.00000000, 10.00000000, 500000.00000000, 1.00000000, 1, 0.001000, 19, 1, 1, 1, 4),
('TRX', 'Tron', 'TRX', 'tron', 'Tron', NULL, 6, 10.00000000, 10000000.00000000, 100.00000000, 5000000.00000000, 1.00000000, 1, 0.001000, 19, 1, 1, 1, 5),
('USDC', 'USD Coin', 'USDC', 'ethereum', 'Ethereum', '0xA0b86a33E6417aAb7b6DbCBbe9FD4E89c0778a4B', 6, 1.00000000, 1000000.00000000, 10.00000000, 500000.00000000, 1.00000000, 1, 0.001000, 12, 1, 1, 1, 6);