-- ======================================================
-- 用户服务数据库初始化脚本（极简版）
-- 创建时间：2025-09-17
-- 功能：只包含基本的表结构，适合微服务架构
-- ======================================================

-- 创建用户服务数据库
CREATE DATABASE IF NOT EXISTS `ppcex_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `ppcex_user`;

-- ======================================================
-- 用户基本信息表 (user_info)
-- ======================================================
CREATE TABLE IF NOT EXISTS `user_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_no` VARCHAR(32) NOT NULL COMMENT '用户编号',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `salt` VARCHAR(64) NOT NULL COMMENT '密码盐值',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `country` VARCHAR(10) DEFAULT 'CN' COMMENT '国家',
    `language` VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言',
    `timezone` VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-正常 2-冻结 3-注销',
    `kyc_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'KYC状态 0-未认证 1-已认证',
    `register_time` DATETIME NOT NULL COMMENT '注册时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
    `google_auth_secret` VARCHAR(32) DEFAULT NULL COMMENT 'Google认证密钥',
    `google_auth_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT 'Google认证启用 0-未启用 1-已启用',
    `login_failed_count` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    `account_locked_until` DATETIME DEFAULT NULL COMMENT '账户锁定到期时间',
    `invite_code` VARCHAR(8) DEFAULT NULL COMMENT '邀请码',
    `inviter_id` BIGINT DEFAULT NULL COMMENT '邀请人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_no` (`user_no`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_status` (`status`),
    KEY `idx_kyc_status` (`kyc_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_inviter_id` (`inviter_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';

-- ======================================================
-- 用户登录日志表 (user_login_log)
-- ======================================================
CREATE TABLE IF NOT EXISTS `user_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `login_type` TINYINT NOT NULL COMMENT '登录类型 1-密码登录 2-短信登录 3-邮箱登录 4-Google登录',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `login_ip` VARCHAR(45) DEFAULT NULL COMMENT '登录IP',
    `login_result` TINYINT NOT NULL COMMENT '登录结果 1-成功 2-失败',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `device_info` VARCHAR(500) DEFAULT NULL COMMENT '设备信息',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '地理位置',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_login_result` (`login_result`),
    KEY `idx_login_ip` (`login_ip`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- ======================================================
-- 用户KYC认证表 (user_kyc)
-- ======================================================
CREATE TABLE IF NOT EXISTS `user_kyc` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'KYC ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `id_type` TINYINT DEFAULT NULL COMMENT '证件类型 1-身份证 2-护照 3-驾驶证',
    `id_number` VARCHAR(50) DEFAULT NULL COMMENT '证件号码',
    `id_front_url` VARCHAR(255) DEFAULT NULL COMMENT '证件正面照片URL',
    `id_back_url` VARCHAR(255) DEFAULT NULL COMMENT '证件背面照片URL',
    `selfie_url` VARCHAR(255) DEFAULT NULL COMMENT '自拍照URL',
    `nationality` VARCHAR(50) DEFAULT NULL COMMENT '国籍',
    `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `country` VARCHAR(50) DEFAULT NULL COMMENT '国家',
    `postal_code` VARCHAR(20) DEFAULT NULL COMMENT '邮政编码',
    `kyc_level` TINYINT NOT NULL DEFAULT 0 COMMENT 'KYC等级 0-未认证 1-基础认证 2-高级认证',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待审核 1-已通过 2-已拒绝',
    `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_kyc_level` (`kyc_level`),
    KEY `idx_submit_time` (`submit_time`),
    KEY `idx_audit_time` (`audit_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户KYC认证表';

-- ======================================================
-- 用户资产表 (user_asset)
-- ======================================================
CREATE TABLE IF NOT EXISTS `user_asset` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资产ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `asset_type` TINYINT NOT NULL COMMENT '资产类型 1-法币 2-数字货币',
    `currency` VARCHAR(20) NOT NULL COMMENT '货币类型',
    `available_balance` DECIMAL(20,8) NOT NULL DEFAULT 0.00000000 COMMENT '可用余额',
    `frozen_balance` DECIMAL(20,8) NOT NULL DEFAULT 0.00000000 COMMENT '冻结余额',
    `total_balance` DECIMAL(20,8) NOT NULL DEFAULT 0.00000000 COMMENT '总余额',
    `wallet_address` VARCHAR(255) DEFAULT NULL COMMENT '钱包地址',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-正常 2-冻结',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_currency` (`user_id`, `currency`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_currency` (`currency`),
    KEY `idx_asset_type` (`asset_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资产表';

-- ======================================================
-- 插入初始数据（可选）
-- ======================================================

-- 插入默认的数字货币资产配置（系统用户）
INSERT IGNORE INTO `user_asset` (`user_id`, `asset_type`, `currency`, `available_balance`, `frozen_balance`, `total_balance`, `status`) VALUES
(0, 2, 'BTC', 0.00000000, 0.00000000, 0.00000000, 1),
(0, 2, 'ETH', 0.00000000, 0.00000000, 0.00000000, 1),
(0, 2, 'USDT', 0.00000000, 0.00000000, 0.00000000, 1),
(0, 1, 'USD', 0.00, 0.00, 0.00, 1),
(0, 1, 'CNY', 0.00, 0.00, 0.00, 1);

-- ======================================================
-- 数据库初始化完成
-- ======================================================

SELECT 'User service database initialization completed successfully!' as message;