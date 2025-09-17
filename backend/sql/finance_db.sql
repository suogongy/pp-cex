-- 财务系统数据库初始化脚本

CREATE DATABASE IF NOT EXISTS finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE finance_db;

-- 资金流水表
CREATE TABLE `financial_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `flow_no` varchar(32) NOT NULL COMMENT '流水编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(32) NOT NULL COMMENT '币种ID',
  `coin_name` varchar(50) NOT NULL COMMENT '币种名称',
  `business_type` tinyint(1) NOT NULL COMMENT '业务类型 1-充值 2-提现 3-买入 4-卖出 5-手续费 6-转账 99-其他',
  `amount` decimal(20,8) NOT NULL COMMENT '金额',
  `balance_before` decimal(20,8) NOT NULL COMMENT '操作前余额',
  `balance_after` decimal(20,8) NOT NULL COMMENT '操作后余额',
  `fee` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '手续费',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-成功 2-失败 3-处理中',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `ref_order_no` varchar(32) DEFAULT NULL COMMENT '关联订单号',
  `ref_tx_hash` varchar(255) DEFAULT NULL COMMENT '关联交易哈希',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coin_id` (`coin_id`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_status` (`status`),
  KEY `idx_ref_order_no` (`ref_order_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

-- 财务日报表
CREATE TABLE `financial_daily_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `report_date` date NOT NULL COMMENT '报表日期',
  `total_users` bigint(20) DEFAULT '0' COMMENT '总用户数',
  `active_users` bigint(20) DEFAULT '0' COMMENT '活跃用户数',
  `new_users` bigint(20) DEFAULT '0' COMMENT '新增用户数',
  `total_trades` bigint(20) DEFAULT '0' COMMENT '总交易笔数',
  `total_trade_volume` decimal(20,8) DEFAULT '0.00000000' COMMENT '总交易量',
  `total_fee_income` decimal(20,8) DEFAULT '0.00000000' COMMENT '总手续费收入',
  `total_recharge` decimal(20,8) DEFAULT '0.00000000' COMMENT '总充值金额',
  `total_withdraw` decimal(20,8) DEFAULT '0.00000000' COMMENT '总提现金额',
  `net_deposit` decimal(20,8) DEFAULT '0.00000000' COMMENT '净入金',
  `total_assets` decimal(20,8) DEFAULT '0.00000000' COMMENT '总资产',
  `hot_wallet_balance` decimal(20,8) DEFAULT '0.00000000' COMMENT '热钱包余额',
  `cold_wallet_balance` decimal(20,8) DEFAULT '0.00000000' COMMENT '冷钱包余额',
  `risk_events_count` int(11) DEFAULT '0' COMMENT '风险事件数',
  `report_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '报表状态 1-草稿 2-已确认 3-已审核 4-已发布',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_date` (`report_date`),
  KEY `idx_report_status` (`report_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务日报表';

-- 风控规则表
CREATE TABLE `risk_control` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` tinyint(1) NOT NULL COMMENT '规则类型 1-IP黑名单 2-频率限制 3-金额限制 4-行为模式 5-地理位置风险 6-设备风险 7-时间风险',
  `rule_code` varchar(50) NOT NULL COMMENT '规则代码',
  `description` varchar(500) DEFAULT NULL COMMENT '规则描述',
  `threshold_value` decimal(20,8) DEFAULT NULL COMMENT '阈值',
  `threshold_type` varchar(20) DEFAULT NULL COMMENT '阈值类型',
  `action_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '处理动作 1-告警 2-冻结 3-拒绝 4-人工审核 5-限制访问',
  `risk_level` tinyint(1) NOT NULL DEFAULT '2' COMMENT '风险等级 1-低 2-中 3-高 4-严重',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用 1-启用 0-禁用',
  `priority` int(11) NOT NULL DEFAULT '0' COMMENT '优先级',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 风险事件表
CREATE TABLE `risk_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `event_no` varchar(32) NOT NULL COMMENT '事件编号',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `event_type` tinyint(1) NOT NULL COMMENT '事件类型 1-登录异常 2-交易异常 3-提现异常 4-充值异常 5-行为异常 6-设备异常 7-IP异常 8-金额异常',
  `risk_level` tinyint(1) NOT NULL DEFAULT '1' COMMENT '风险等级 1-低 2-中 3-高 4-严重',
  `event_content` varchar(1000) NOT NULL COMMENT '事件内容',
  `related_data` text DEFAULT NULL COMMENT '相关数据',
  `client_ip` varchar(45) DEFAULT NULL COMMENT '客户端IP',
  `device_info` varchar(500) DEFAULT NULL COMMENT '设备信息',
  `location` varchar(100) DEFAULT NULL COMMENT '地理位置',
  `amount` decimal(20,8) DEFAULT NULL COMMENT '金额',
  `coin_id` varchar(32) DEFAULT NULL COMMENT '币种ID',
  `rule_triggered` varchar(500) DEFAULT NULL COMMENT '触发规则',
  `action_taken` varchar(500) DEFAULT NULL COMMENT '处理动作',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-待处理 2-处理中 3-已解决 4-已忽略 5-已升级',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_user` varchar(32) DEFAULT NULL COMMENT '处理人',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_no` (`event_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险事件表';

-- 插入默认风控规则
INSERT INTO `risk_control` (`rule_name`, `rule_type`, `rule_code`, `description`, `threshold_value`, `threshold_type`, `action_type`, `risk_level`, `enabled`, `priority`) VALUES
('IP黑名单检查', 1, 'IP_BLACKLIST', '检查IP是否在黑名单中', NULL, NULL, 2, 4, 1, 10),
('登录频率限制', 2, 'LOGIN_RATE_LIMIT', '限制登录频率', 5, 'count_per_minute', 1, 2, 1, 20),
('交易频率限制', 2, 'TRADE_RATE_LIMIT', '限制交易频率', 10, 'count_per_minute', 1, 2, 1, 15),
('大额交易限制', 3, 'LARGE_AMOUNT_LIMIT', '大额交易风控', 10000.00000000, 'USDT', 2, 3, 1, 25),
('异地登录检测', 5, 'UNUSUAL_LOCATION', '检测异地登录', NULL, NULL, 1, 2, 1, 30),
('高风险地区限制', 5, 'HIGH_RISK_REGION', '高风险地区限制', NULL, NULL, 2, 3, 1, 35),
('设备异常检测', 6, 'DEVICE_ANOMALY', '设备异常检测', NULL, NULL, 1, 2, 1, 40),
('异常时间操作', 7, 'ABNORMAL_TIME', '异常时间操作检测', NULL, NULL, 1, 2, 1, 45);

-- 创建数据库用户
CREATE USER IF NOT EXISTS 'finance_user'@'%' IDENTIFIED BY 'finance_password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, ALTER, CREATE ROUTINE, ALTER ROUTINE ON finance_db.* TO 'finance_user'@'%';
FLUSH PRIVILEGES;

-- 显示创建结果
SELECT 'Finance database initialized successfully' as status;