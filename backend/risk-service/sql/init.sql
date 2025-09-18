-- 风控服务数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS risk_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE risk_db;

-- 删除已存在的表
DROP TABLE IF EXISTS `user_behavior_log`;
DROP TABLE IF EXISTS `risk_statistics`;
DROP TABLE IF EXISTS `risk_whitelist`;
DROP TABLE IF EXISTS `risk_strategy`;
DROP TABLE IF EXISTS `user_risk_status`;
DROP TABLE IF EXISTS `risk_event`;
DROP TABLE IF EXISTS `risk_rule`;

-- 创建风控规则表
CREATE TABLE `risk_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '风控规则ID',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` tinyint(1) NOT NULL COMMENT '规则类型 1-用户风控 2-交易风控 3-资产风控 4-系统风控',
  `rule_category` varchar(50) NOT NULL COMMENT '规则分类',
  `risk_level` tinyint(1) NOT NULL DEFAULT '1' COMMENT '风险等级 1-低 2-中 3-高 4-严重',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-启用 2-禁用',
  `rule_expression` text NOT NULL COMMENT '规则表达式(JSON格式)',
  `threshold_value` decimal(20,8) DEFAULT NULL COMMENT '阈值',
  `action_type` tinyint(1) NOT NULL COMMENT '动作类型 1-警告 2-限制 3-冻结 4-拒绝',
  `action_params` text DEFAULT NULL COMMENT '动作参数(JSON格式)',
  `description` varchar(500) DEFAULT NULL COMMENT '规则描述',
  `priority` int(11) NOT NULL DEFAULT '0' COMMENT '优先级',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_rule_category` (`rule_category`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 创建风控事件表
CREATE TABLE `risk_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '风控事件ID',
  `event_no` varchar(32) NOT NULL COMMENT '事件编号',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `rule_id` bigint(20) NOT NULL COMMENT '规则ID',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码',
  `event_type` tinyint(1) NOT NULL COMMENT '事件类型 1-登录 2-交易 3-充值 4-提现 5-注册 6-其他',
  `risk_level` tinyint(1) NOT NULL COMMENT '风险等级 1-低 2-中 3-高 4-严重',
  `event_data` text NOT NULL COMMENT '事件数据(JSON格式)',
  `trigger_value` decimal(20,8) DEFAULT NULL COMMENT '触发值',
  `threshold_value` decimal(20,8) DEFAULT NULL COMMENT '阈值',
  `action_type` tinyint(1) NOT NULL COMMENT '动作类型 1-警告 2-限制 3-冻结 4-拒绝',
  `action_result` text DEFAULT NULL COMMENT '动作结果',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-待处理 2-处理中 3-已处理 4-已忽略',
  `processing_time` datetime DEFAULT NULL COMMENT '处理时间',
  `processor` varchar(32) DEFAULT NULL COMMENT '处理人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_no` (`event_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_rule_code` (`rule_code`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控事件表';

-- 创建用户风控状态表
CREATE TABLE `user_risk_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `risk_score` int(11) NOT NULL DEFAULT '0' COMMENT '风险评分(0-100)',
  `risk_level` tinyint(1) NOT NULL DEFAULT '1' COMMENT '风险等级 1-低 2-中 3-高 4-严重',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-正常 2-监控 3-限制 4-冻结',
  `freeze_reason` varchar(500) DEFAULT NULL COMMENT '冻结原因',
  `freeze_time` datetime DEFAULT NULL COMMENT '冻结时间',
  `unfreeze_time` datetime DEFAULT NULL COMMENT '解冻时间',
  `last_risk_time` datetime DEFAULT NULL COMMENT '最后风控时间',
  `risk_event_count` int(11) NOT NULL DEFAULT '0' COMMENT '风控事件数量',
  `total_login_count` int(11) NOT NULL DEFAULT '0' COMMENT '总登录次数',
  `failed_login_count` int(11) NOT NULL DEFAULT '0' COMMENT '失败登录次数',
  `suspicious_ip_count` int(11) NOT NULL DEFAULT '0' COMMENT '可疑IP数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_risk_score` (`risk_score`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_status` (`status`),
  KEY `idx_last_risk_time` (`last_risk_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户风控状态表';

-- 创建风控策略表
CREATE TABLE `risk_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `strategy_code` varchar(50) NOT NULL COMMENT '策略编码',
  `strategy_name` varchar(100) NOT NULL COMMENT '策略名称',
  `strategy_type` tinyint(1) NOT NULL COMMENT '策略类型 1-用户策略 2-交易策略 3-资产策略',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-启用 2-禁用',
  `priority` int(11) NOT NULL DEFAULT '0' COMMENT '优先级(数字越小优先级越高)',
  `rule_ids` text NOT NULL COMMENT '规则ID列表(JSON数组)',
  `match_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '匹配类型 1-任意匹配 2-全部匹配 3-加权匹配',
  `action_type` tinyint(1) NOT NULL COMMENT '动作类型 1-警告 2-限制 3-冻结 4-拒绝',
  `action_params` text DEFAULT NULL COMMENT '动作参数(JSON格式)',
  `description` varchar(500) DEFAULT NULL COMMENT '策略描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_strategy_code` (`strategy_code`),
  KEY `idx_strategy_type` (`strategy_type`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控策略表';

-- 创建风控白名单表
CREATE TABLE `risk_whitelist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '白名单ID',
  `whitelist_type` tinyint(1) NOT NULL COMMENT '白名单类型 1-用户 2-IP 3-设备 4-地址',
  `whitelist_value` varchar(255) NOT NULL COMMENT '白名单值',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `effective_time` datetime NOT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1-启用 2-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_whitelist_type` (`whitelist_type`),
  KEY `idx_whitelist_value` (`whitelist_value`),
  KEY `idx_status` (`status`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控白名单表';

-- 创建风控统计表
CREATE TABLE `risk_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_events` int(11) NOT NULL DEFAULT '0' COMMENT '总事件数',
  `high_risk_events` int(11) NOT NULL DEFAULT '0' COMMENT '高风险事件数',
  `medium_risk_events` int(11) NOT NULL DEFAULT '0' COMMENT '中风险事件数',
  `low_risk_events` int(11) NOT NULL DEFAULT '0' COMMENT '低风险事件数',
  `processed_events` int(11) NOT NULL DEFAULT '0' COMMENT '已处理事件数',
  `pending_events` int(11) NOT NULL DEFAULT '0' COMMENT '待处理事件数',
  `blocked_users` int(11) NOT NULL DEFAULT '0' COMMENT '被阻止用户数',
  `frozen_users` int(11) NOT NULL DEFAULT '0' COMMENT '被冻结用户数',
  `total_blocked_amount` decimal(20,8) NOT NULL DEFAULT '0.00000000' COMMENT '总阻止金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控统计表';

-- 创建用户行为记录表
CREATE TABLE `user_behavior_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `behavior_type` tinyint(1) NOT NULL COMMENT '行为类型 1-登录 2-交易 3-充值 4-提现 5-查看 6-其他',
  `action` varchar(50) NOT NULL COMMENT '操作动作',
  `device_info` text DEFAULT NULL COMMENT '设备信息(JSON格式)',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(100) DEFAULT NULL COMMENT '地理位置',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_params` text DEFAULT NULL COMMENT '请求参数',
  `response_status` int(11) DEFAULT NULL COMMENT '响应状态',
  `response_time` int(11) DEFAULT NULL COMMENT '响应时间(ms)',
  `risk_score` int(11) DEFAULT '0' COMMENT '风险评分',
  `is_suspicious` tinyint(1) DEFAULT '0' COMMENT '是否可疑 0-正常 1-可疑',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_action` (`action`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_is_suspicious` (`is_suspicious`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_risk_score` (`risk_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- 插入基础风控规则数据
INSERT INTO `risk_rule` (`rule_code`, `rule_name`, `rule_type`, `rule_category`, `risk_level`, `status`, `rule_expression`, `threshold_value`, `action_type`, `description`, `priority`) VALUES
('LOGIN_FAILED_3_TIMES', '登录失败3次', 1, '登录安全', 2, 1, '{"condition": "LOGIN_FAILED_COUNT", "count": 3}', 3, 1, '用户连续登录失败3次触发警告', 10),
('LOGIN_FAILED_5_TIMES', '登录失败5次', 1, '登录安全', 3, 1, '{"condition": "LOGIN_FAILED_COUNT", "count": 5}', 5, 2, '用户连续登录失败5次触发限制', 20),
('LARGE_AMOUNT_WITHDRAW', '大额提现', 3, '资金安全', 3, 1, '{"condition": "AMOUNT_GT", "currency": "USDT", "amount": 10000}', 10000, 2, '单次提现超过10000 USDT触发限制', 15),
('HIGH_FREQUENCY_TRADE', '高频交易', 2, '交易安全', 2, 1, '{"condition": "FREQUENCY_GT", "interval": "1m", "count": 10}', 10, 1, '1分钟内交易超过10次触发警告', 12),
('NEW_USER_LARGE_TRADE', '新用户大额交易', 2, '交易安全', 3, 1, '{"condition": "NEW_USER_AMOUNT_GT", "days": 7, "amount": 5000}', 5000, 3, '注册7天内单次交易超过5000 USDT触发冻结', 25),
('SUSPICIOUS_IP_LOGIN', '可疑IP登录', 1, '登录安全', 2, 1, '{"condition": "IP_BLACKLIST"}', 0, 2, '使用黑名单IP登录触发限制', 18),
('NIGHT_LARGE_WITHDRAW', '夜间大额提现', 3, '资金安全', 3, 1, '{"condition": "TIME_RANGE", "start": "23:00", "end": "06:00", "amount": 5000}', 5000, 3, '夜间23点至6点提现超过5000 USDT触发冻结', 22),
('MULTI_DEVICE_LOGIN', '多设备登录', 1, '登录安全', 2, 1, '{"condition": "DEVICE_COUNT", "count": 3, "hours": 24}', 3, 2, '24小时内登录设备超过3个触发限制', 16),
('PRICE_DEVIATION_LARGE', '价格偏离过大', 2, '交易安全', 3, 1, '{"condition": "PRICE_DEVIATION", "threshold": 0.1}', 0.1, 3, '交易价格偏离市场10%以上触发冻结', 20),
('KYC_NOT_COMPLETED', 'KYC未完成', 1, '用户安全', 2, 1, '{"condition": "KYC_STATUS", "status": "NOT_COMPLETED"}', 0, 2, 'KYC未完成用户触发限制', 8);

-- 插入基础风控策略数据
INSERT INTO `risk_strategy` (`strategy_code`, `strategy_name`, `strategy_type`, `status`, `priority`, `rule_ids`, `match_type`, `action_type`, `description`) VALUES
('USER_LOGIN_STRATEGY', '用户登录策略', 1, 1, 1, '[1, 2]', 2, 1, '用户登录风控策略，需要同时触发多个规则'),
('ASSET_WITHDRAW_STRATEGY', '资产提现策略', 3, 1, 2, '[3, 7]', 1, 3, '资产提现风控策略，触发任意规则即冻结'),
('TRADE_SECURITY_STRATEGY', '交易安全策略', 2, 1, 3, '[4, 9]', 1, 2, '交易安全风控策略，触发任意规则即限制'),
('NEW_USER_STRATEGY', '新用户策略', 1, 1, 4, '[5, 10]', 2, 2, '新用户风控策略，需要同时触发两个规则');

-- 插入基础白名单数据
INSERT INTO `risk_whitelist` (`whitelist_type`, `whitelist_value`, `description`, `effective_time`, `status`) VALUES
(2, '192.168.1.0/24', '内网IP段', NOW(), 1),
(2, '127.0.0.1', '本地回环地址', NOW(), 1),
(1, '10001', '测试用户', NOW(), 1),
(1, '10002', '管理员用户', NOW(), 1);

-- 插入基础用户风控状态数据
INSERT INTO `user_risk_status` (`user_id`, `risk_score`, `risk_level`, `status`) VALUES
(10001, 5, 1, 1),
(10002, 0, 1, 1),
(10003, 25, 2, 2);

-- 创建索引优化查询性能
CREATE INDEX idx_risk_event_composite ON risk_event(user_id, event_type, create_time);
CREATE INDEX idx_user_behavior_composite ON user_behavior_log(user_id, behavior_type, create_time);
CREATE INDEX idx_whitelist_composite ON risk_whitelist(whitelist_type, whitelist_value, status, effective_time, expire_time);

-- 创建视图用于查询统计
CREATE VIEW v_risk_daily_statistics AS
SELECT
    DATE(create_time) as stat_date,
    COUNT(*) as total_events,
    SUM(CASE WHEN risk_level = 3 THEN 1 ELSE 0 END) as high_risk_events,
    SUM(CASE WHEN risk_level = 2 THEN 1 ELSE 0 END) as medium_risk_events,
    SUM(CASE WHEN risk_level = 1 THEN 1 ELSE 0 END) as low_risk_events,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as processed_events,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as pending_events
FROM risk_event
WHERE deleted = 0
GROUP BY DATE(create_time);

-- 创建存储过程用于数据统计
DELIMITER //
CREATE PROCEDURE sp_generate_daily_statistics(IN stat_date DATE)
BEGIN
    DECLARE total_count INT;
    DECLARE high_risk_count INT;
    DECLARE medium_risk_count INT;
    DECLARE low_risk_count INT;
    DECLARE processed_count INT;
    DECLARE pending_count INT;

    -- 计算统计数据
    SELECT COUNT(*) INTO total_count FROM risk_event
    WHERE DATE(create_time) = stat_date AND deleted = 0;

    SELECT COUNT(*) INTO high_risk_count FROM risk_event
    WHERE DATE(create_time) = stat_date AND risk_level = 3 AND deleted = 0;

    SELECT COUNT(*) INTO medium_risk_count FROM risk_event
    WHERE DATE(create_time) = stat_date AND risk_level = 2 AND deleted = 0;

    SELECT COUNT(*) INTO low_risk_count FROM risk_event
    WHERE DATE(create_time) = stat_date AND risk_level = 1 AND deleted = 0;

    SELECT COUNT(*) INTO processed_count FROM risk_event
    WHERE DATE(create_time) = stat_date AND status = 3 AND deleted = 0;

    SELECT COUNT(*) INTO pending_count FROM risk_event
    WHERE DATE(create_time) = stat_date AND status = 1 AND deleted = 0;

    -- 插入或更新统计记录
    INSERT INTO risk_statistics (
        stat_date, total_events, high_risk_events, medium_risk_events,
        low_risk_events, processed_events, pending_events
    ) VALUES (
        stat_date, total_count, high_risk_count, medium_risk_count,
        low_risk_count, processed_count, pending_count
    ) ON DUPLICATE KEY UPDATE
        total_events = VALUES(total_events),
        high_risk_events = VALUES(high_risk_events),
        medium_risk_events = VALUES(medium_risk_events),
        low_risk_events = VALUES(low_risk_events),
        processed_events = VALUES(processed_events),
        pending_events = VALUES(pending_events),
        update_time = NOW();
END //
DELIMITER ;

-- 创建定时任务事件
CREATE EVENT IF NOT EXISTS event_daily_statistics
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP + INTERVAL 1 HOUR
DO
BEGIN
    CALL sp_generate_daily_statistics(CURDATE() - INTERVAL 1 DAY);
END;

-- 设置数据库字符集和排序规则
ALTER DATABASE risk_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
-- CREATE USER 'risk_user'@'%' IDENTIFIED BY 'risk_password_2024';
-- GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON risk_db.* TO 'risk_user'@'%';
-- FLUSH PRIVILEGES;

-- 显示初始化完成信息
SELECT 'Risk Service Database Initialized Successfully' as message;