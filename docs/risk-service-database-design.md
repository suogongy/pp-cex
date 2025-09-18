# Risk Service 数据库设计

## 1. 风控规则表 (risk_rule)
```sql
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
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_rule_category` (`rule_category`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';
```

## 2. 风控事件表 (risk_event)
```sql
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
```

## 3. 用户风控状态表 (user_risk_status)
```sql
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_risk_score` (`risk_score`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_status` (`status`),
  KEY `idx_last_risk_time` (`last_risk_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户风控状态表';
```

## 4. 风控策略表 (risk_strategy)
```sql
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_strategy_code` (`strategy_code`),
  KEY `idx_strategy_type` (`strategy_type`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控策略表';
```

## 5. 风控白名单表 (risk_whitelist)
```sql
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
  PRIMARY KEY (`id`),
  KEY `idx_whitelist_type` (`whitelist_type`),
  KEY `idx_whitelist_value` (`whitelist_value`),
  KEY `idx_status` (`status`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控白名单表';
```

## 6. 风控统计表 (risk_statistics)
```sql
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控统计表';
```

## 7. 用户行为记录表 (user_behavior_log)
```sql
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
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_action` (`action`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_is_suspicious` (`is_suspicious`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_risk_score` (`risk_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';
```