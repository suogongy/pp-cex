-- 创建通知服务数据库
CREATE DATABASE IF NOT EXISTS notify_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE notify_db;

-- 通知配置表
CREATE TABLE IF NOT EXISTS `notify_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_type` tinyint(1) NOT NULL COMMENT '配置类型 1-邮件 2-短信 3-站内信 4-推送 5-Webhook',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值(JSON格式)',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-启用 2-禁用',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知配置表';

-- 通知模板表
CREATE TABLE IF NOT EXISTS `notify_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_code` varchar(50) NOT NULL COMMENT '模板编码',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_type` tinyint(1) NOT NULL COMMENT '模板类型 1-邮件 2-短信 3-站内信 4-推送',
  `template_content` text NOT NULL COMMENT '模板内容',
  `template_vars` text DEFAULT NULL COMMENT '模板变量(JSON格式)',
  `language` varchar(10) NOT NULL DEFAULT 'zh-CN' COMMENT '语言',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-启用 2-禁用',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code_lang` (`template_code`, `language`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知模板表';

-- 通知记录表
CREATE TABLE IF NOT EXISTS `notify_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `notify_no` varchar(32) NOT NULL COMMENT '通知编号',
  `business_type` tinyint(1) NOT NULL COMMENT '业务类型 1-订单 2-交易 3-资产 4-安全 5-系统',
  `notify_type` tinyint(1) NOT NULL COMMENT '通知类型 1-邮件 2-短信 3-站内信 4-推送',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `recipient` varchar(255) NOT NULL COMMENT '接收者',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `template_code` varchar(50) DEFAULT NULL COMMENT '模板编码',
  `template_vars` text DEFAULT NULL COMMENT '模板变量(JSON格式)',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-待发送 2-发送中 3-已发送 4-发送失败',
  `send_count` int(11) NOT NULL DEFAULT '0' COMMENT '发送次数',
  `max_retry` int(11) NOT NULL DEFAULT '3' COMMENT '最大重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `error_msg` text DEFAULT NULL COMMENT '错误信息',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notify_no` (`notify_no`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_notify_type` (`notify_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_next_retry_time` (`next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- 用户通知偏好表
CREATE TABLE IF NOT EXISTS `user_notify_preference` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `notify_type` tinyint(1) NOT NULL COMMENT '通知类型 1-邮件 2-短信 3-站内信 4-推送',
  `business_type` tinyint(1) NOT NULL COMMENT '业务类型 1-订单 2-交易 3-资产 4-安全 5-系统',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用 1-启用 0-禁用',
  `contact_info` varchar(255) DEFAULT NULL COMMENT '联系方式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_notify_business` (`user_id`, `notify_type`, `business_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_notify_type` (`notify_type`),
  KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知偏好表';

-- 通知统计表
CREATE TABLE IF NOT EXISTS `notify_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `notify_type` tinyint(1) NOT NULL COMMENT '通知类型 1-邮件 2-短信 3-站内信 4-推送',
  `business_type` tinyint(1) NOT NULL COMMENT '业务类型 1-订单 2-交易 3-资产 4-安全 5-系统',
  `total_count` int(11) NOT NULL DEFAULT '0' COMMENT '总数',
  `success_count` int(11) NOT NULL DEFAULT '0' COMMENT '成功数',
  `fail_count` int(11) NOT NULL DEFAULT '0' COMMENT '失败数',
  `success_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '成功率',
  `avg_response_time` int(11) NOT NULL DEFAULT '0' COMMENT '平均响应时间(毫秒)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_type_business` (`stat_date`, `notify_type`, `business_type`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_notify_type` (`notify_type`),
  KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知统计表';

-- 插入初始数据

-- 通知配置数据
INSERT INTO `notify_config` (`config_type`, `config_name`, `config_key`, `config_value`, `status`, `description`) VALUES
(1, '邮件服务器配置', 'email_smtp_config', '{"host": "smtp.gmail.com", "port": 587, "username": "your-email@gmail.com", "password": "your-password", "protocol": "smtp"}', 1, '邮件服务器配置'),
(2, '短信服务配置', 'sms_service_config', '{"provider": "aliyun", "access_key": "your-access-key", "access_secret": "your-access-secret", "sign_name": "PPCEX", "template_code": "SMS_123456789"}', 1, '短信服务配置'),
(3, '站内信配置', 'in_app_config', '{"storage": "redis", "expire_hours": 168, "max_count": 100}', 1, '站内信配置');

-- 通知模板数据
INSERT INTO `notify_template` (`template_code`, `template_name`, `template_type`, `template_content`, `template_vars`, `language`, `status`, `description`) VALUES
('ORDER_CREATE', '订单创建通知', 1, '<h3>订单创建成功</h3><p>尊敬的${userName}，您的订单已成功创建。</p><p>订单号：${orderNo}</p><p>交易对：${symbol}</p><p>数量：${amount}</p><p>价格：${price}</p><p>请及时关注订单状态。</p>', '["userName", "orderNo", "symbol", "amount", "price"]', 'zh-CN', 1, '订单创建邮件模板'),
('ORDER_CREATE', '订单创建通知', 2, '【PPCEX】您的订单${orderNo}已创建，交易对：${symbol}，数量：${amount}，价格：${price}', '["orderNo", "symbol", "amount", "price"]', 'zh-CN', 1, '订单创建短信模板'),
('ORDER_CREATE', '订单创建通知', 3, '您的订单${orderNo}已创建，交易对：${symbol}，数量：${amount}，价格：${price}', '["orderNo", "symbol", "amount", "price"]', 'zh-CN', 1, '订单创建站内信模板'),
('TRADE_SUCCESS', '交易成功通知', 1, '<h3>交易成功</h3><p>尊敬的${userName}，您的交易已成功完成。</p><p>订单号：${orderNo}</p><p>交易对：${symbol}</p><p>成交数量：${amount}</p><p>成交价格：${price}</p><p>手续费：${fee}</p>', '["userName", "orderNo", "symbol", "amount", "price", "fee"]', 'zh-CN', 1, '交易成功邮件模板'),
('TRADE_SUCCESS', '交易成功通知', 2, '【PPCEX】您的订单${orderNo}交易成功，成交数量：${amount}，成交价格：${price}', '["orderNo", "amount", "price"]', 'zh-CN', 1, '交易成功短信模板'),
('ASSET_CHANGE', '资产变动通知', 1, '<h3>资产变动通知</h3><p>尊敬的${userName}，您的资产发生变动。</p><p>币种：${coinName}</p><p>变动数量：${amount}</p><p>变动类型：${changeType}</p><p>余额：${balance}</p>', '["userName", "coinName", "amount", "changeType", "balance"]', 'zh-CN', 1, '资产变动邮件模板'),
('SECURITY_ALERT', '安全告警通知', 1, '<h3>安全告警</h3><p>尊敬的${userName}，检测到您的账户存在安全风险。</p><p>告警类型：${alertType}</p><p>发生时间：${alertTime}</p><p>请及时检查您的账户安全。</p>', '["userName", "alertType", "alertTime"]', 'zh-CN', 1, '安全告警邮件模板'),
('SECURITY_ALERT', '安全告警通知', 2, '【PPCEX】安全告警：您的账户存在${alertType}风险，请及时处理', '["alertType"]', 'zh-CN', 1, '安全告警短信模板');

-- 用户通知偏好数据（示例）
INSERT INTO `user_notify_preference` (`user_id`, `notify_type`, `business_type`, `enabled`, `contact_info`) VALUES
(1, 1, 1, 1, 'user1@example.com'),  -- 用户1启用订单邮件通知
(1, 2, 1, 0, '13800138000'),        -- 用户1禁用订单短信通知
(1, 1, 2, 1, 'user1@example.com'),  -- 用户1启用交易邮件通知
(1, 3, 1, 1, NULL),                -- 用户1启用订单站内信通知
(1, 3, 2, 1, NULL),                -- 用户1启用交易站内信通知
(1, 1, 4, 1, 'user1@example.com'),  -- 用户1启用安全邮件通知
(2, 1, 1, 1, 'user2@example.com'),  -- 用户2启用订单邮件通知
(2, 2, 1, 1, '13800138001'),        -- 用户2启用订单短信通知
(2, 3, 1, 1, NULL);                -- 用户2启用订单站内信通知