package com.ppcex.risk.constant;

/**
 * 风控服务常量定义
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class RiskConstants {

    /**
     * 缓存键前缀
     */
    public static final String CACHE_PREFIX = "risk:";

    /**
     * 规则缓存键
     */
    public static final String RULE_CACHE_KEY = CACHE_PREFIX + "rule:";

    /**
     * 策略缓存键
     */
    public static final String STRATEGY_CACHE_KEY = CACHE_PREFIX + "strategy:";

    /**
     * 白名单缓存键
     */
    public static final String WHITELIST_CACHE_KEY = CACHE_PREFIX + "whitelist:";

    /**
     * 用户风险评分缓存键
     */
    public static final String USER_RISK_SCORE_KEY = CACHE_PREFIX + "user:risk:score:";

    /**
     * 用户风控状态缓存键
     */
    public static final String USER_RISK_STATUS_KEY = CACHE_PREFIX + "user:risk:status:";

    /**
     * 分布式锁键前缀
     */
    public static final String LOCK_PREFIX = "risk:lock:";

    /**
     * 消息幂等性键前缀
     */
    public static final String IDEMPOTENT_PREFIX = "risk:idempotent:";

    /**
     * 规则类型
     */
    public static class RuleType {
        public static final int USER_RISK = 1;      // 用户风控
        public static final int TRADE_RISK = 2;     // 交易风控
        public static final int ASSET_RISK = 3;     // 资产风控
        public static final int SYSTEM_RISK = 4;    // 系统风控
    }

    /**
     * 风险等级
     */
    public static class RiskLevel {
        public static final int LOW = 1;      // 低风险
        public static final int MEDIUM = 2;  // 中风险
        public static final int HIGH = 3;    // 高风险
        public static final int SEVERE = 4;  // 严重风险
    }

    /**
     * 风控动作类型
     */
    public static class ActionType {
        public static final int WARNING = 1;    // 警告
        public static final int LIMIT = 2;     // 限制
        public static final int FREEZE = 3;    // 冻结
        public static final int REJECT = 4;    // 拒绝
        public static final int NONE = 5;      // 无动作
    }

    /**
     * 事件类型
     */
    public static class EventType {
        public static final int LOGIN = 1;      // 登录
        public static final int TRADE = 2;     // 交易
        public static final int RECHARGE = 3;   // 充值
        public static final int WITHDRAW = 4;   // 提现
        public static final int REGISTER = 5;   // 注册
        public static final int OTHER = 6;      // 其他
    }

    /**
     * 状态类型
     */
    public static class Status {
        public static final int ENABLED = 1;    // 启用
        public static final int DISABLED = 2;   // 禁用
        public static final int PENDING = 1;    // 待处理
        public static final int PROCESSING = 2; // 处理中
        public static final int PROCESSED = 3;  // 已处理
        public static final int IGNORED = 4;    // 已忽略
    }

    /**
     * 用户风控状态
     */
    public static class UserRiskStatus {
        public static final int NORMAL = 1;    // 正常
        public static final int MONITOR = 2;   // 监控
        public static final int LIMIT = 3;     // 限制
        public static final int FREEZE = 4;    // 冻结
    }

    /**
     * 白名单类型
     */
    public static class WhitelistType {
        public static final int USER = 1;      // 用户
        public static final int IP = 2;        // IP
        public static final int DEVICE = 3;    // 设备
        public static final int ADDRESS = 4;   // 地址
    }

    /**
     * 行为类型
     */
    public static class BehaviorType {
        public static final int LOGIN = 1;      // 登录
        public static final int TRADE = 2;     // 交易
        public static final int RECHARGE = 3;   // 充值
        public static final int WITHDRAW = 4;   // 提现
        public static final int VIEW = 5;      // 查看
        public static final int OTHER = 6;      // 其他
    }

    /**
     * 策略匹配类型
     */
    public static class MatchType {
        public static final int ANY = 1;        // 任意匹配
        public static final int ALL = 2;        // 全部匹配
        public static final int WEIGHTED = 3;   // 加权匹配
    }

    /**
     * 策略类型
     */
    public static class StrategyType {
        public static final int USER_STRATEGY = 1;   // 用户策略
        public static final int TRADE_STRATEGY = 2;  // 交易策略
        public static final int ASSET_STRATEGY = 3;  // 资产策略
    }

    /**
     * 时间相关常量
     */
    public static class Time {
        public static final long SECOND = 1000L;               // 1秒
        public static final long MINUTE = 60 * SECOND;         // 1分钟
        public static final long HOUR = 60 * MINUTE;           // 1小时
        public static final long DAY = 24 * HOUR;              // 1天
        public static final long WEEK = 7 * DAY;               // 1周
        public static final long MONTH = 30 * DAY;             // 1月
    }

    /**
     * 风险评分相关常量
     */
    public static class Scoring {
        public static final int MAX_SCORE = 100;               // 最大风险评分
        public static final int MIN_SCORE = 0;                 // 最小风险评分
        public static final double BASE_WEIGHT = 0.4;          // 基础评分权重
        public static final double BEHAVIOR_WEIGHT = 0.3;       // 行为评分权重
        public static final double CONTEXT_WEIGHT = 0.2;       // 上下文评分权重
        public static final double HISTORY_WEIGHT = 0.1;       // 历史评分权重
        public static final double TIME_DECAY_FACTOR = 0.95;    // 时间衰减因子
    }

    /**
     * 默认值
     */
    public static class Default {
        public static final int PAGE_SIZE = 20;                 // 默认分页大小
        public static final int MAX_PAGE_SIZE = 100;            // 最大分页大小
        public static final long LOCK_TIMEOUT = 30000L;         // 锁超时时间
        public static final int MAX_RETRY_TIMES = 3;            // 最大重试次数
        public static final long RETRY_INTERVAL = 5000L;        // 重试间隔
    }

    /**
     * 消息主题
     */
    public static class Topics {
        public static final String RISK_TOPIC = "risk-topic";
        public static final String USER_TOPIC = "user-topic";
        public static final String TRADE_TOPIC = "trade-topic";
        public static final String ASSET_TOPIC = "asset-topic";
        public static final String NOTIFICATION_TOPIC = "notification-topic";
    }

    /**
     * 消息标签
     */
    public static class Tags {
        public static final String RISK_EVENT = "risk-event";
        public static final String RULE_UPDATE = "rule-update";
        public static final String STRATEGY_UPDATE = "strategy-update";
        public static final String USER_REGISTER = "user-register";
        public static final String USER_LOGIN = "user-login";
        public static final String USER_KYC = "user-kyc";
        public static final String USER_UPDATE = "user-update";
        public static final String ORDER_CREATE = "order-create";
        public static final String ORDER_TRADE = "order-trade";
        public static final String ORDER_CANCEL = "order-cancel";
        public static final String ORDER_FAILED = "order-failed";
        public static final String ASSET_RECHARGE = "asset-recharge";
        public static final String ASSET_WITHDRAW = "asset-withdraw";
        public static final String ASSET_TRANSFER = "asset-transfer";
        public static final String ASSET_FREEZE = "asset-freeze";
    }
}