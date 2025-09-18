package com.ppcex.risk.engine.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ppcex.common.core.exception.BusinessException;
import com.ppcex.common.core.utils.IdGenerator;
import com.ppcex.common.core.utils.JsonUtils;
import com.ppcex.risk.constant.RiskConstants;
import com.ppcex.risk.dto.RiskCheckRequest;
import com.ppcex.risk.dto.RiskCheckResponse;
import com.ppcex.risk.engine.RiskEngine;
import com.ppcex.risk.entity.RiskEvent;
import com.ppcex.risk.entity.RiskRule;
import com.ppcex.risk.entity.RiskStrategy;
import com.ppcex.risk.entity.RiskWhitelist;
import com.ppcex.risk.entity.UserRiskStatus;
import com.ppcex.risk.entity.UserBehaviorLog;
import com.ppcex.risk.mapper.RiskRuleMapper;
import com.ppcex.risk.mapper.RiskStrategyMapper;
import com.ppcex.risk.mapper.RiskWhitelistMapper;
import com.ppcex.risk.mapper.UserRiskStatusMapper;
import com.ppcex.risk.mapper.UserBehaviorLogMapper;
import com.ppcex.risk.service.RiskService;
import com.ppcex.risk.utils.RiskScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 默认风控引擎实现
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultRiskEngine implements RiskEngine {

    private final RiskRuleMapper riskRuleMapper;
    private final RiskStrategyMapper riskStrategyMapper;
    private final RiskWhitelistMapper riskWhitelistMapper;
    private final UserRiskStatusMapper userRiskStatusMapper;
    private final UserBehaviorLogMapper userBehaviorLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RiskScoreCalculator riskScoreCalculator;

    // 缓存
    private final Map<String, RiskRule> ruleCache = new ConcurrentHashMap<>();
    private final Map<String, RiskStrategy> strategyCache = new ConcurrentHashMap<>();
    private final Map<String, Set<RiskWhitelist>> whitelistCache = new ConcurrentHashMap<>();

    // 引擎状态
    private volatile EngineStatus status = EngineStatus.STOPPED;
    private volatile LocalDateTime lastRefreshTime;

    @PostConstruct
    public void init() {
        initialize();
    }

    @Override
    public void initialize() {
        try {
            status = EngineStatus.INITIALIZING;
            log.info("风控引擎初始化开始...");

            // 加载规则
            loadRules();

            // 加载策略
            loadStrategies();

            // 加载白名单
            loadWhitelist();

            status = EngineStatus.RUNNING;
            lastRefreshTime = LocalDateTime.now();

            log.info("风控引擎初始化完成，状态: {}", status);
        } catch (Exception e) {
            status = EngineStatus.ERROR;
            log.error("风控引擎初始化失败", e);
            throw new BusinessException("风控引擎初始化失败");
        }
    }

    @Override
    public RiskCheckResponse execute(RiskCheckRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            log.debug("开始执行风控检查，用户ID: {}, 事件类型: {}", request.getUserId(), request.getEventType());

            // 幂等性检查
            if (request.getCheckId() != null) {
                String cacheKey = RiskConstants.IDEMPOTENT_PREFIX + request.getCheckId();
                RiskCheckResponse cachedResponse = (RiskCheckResponse) redisTemplate.opsForValue().get(cacheKey);
                if (cachedResponse != null) {
                    log.debug("风控检查命中缓存，checkId: {}", request.getCheckId());
                    return cachedResponse;
                }
            }

            // 记录用户行为
            logUserBehavior(request);

            // 白名单检查
            if (checkWhitelist(request)) {
                RiskCheckResponse response = buildPassResponse(request);
                cacheResponse(request.getCheckId(), response);
                return response;
            }

            // 用户风控状态检查
            UserRiskStatus userRiskStatus = getUserRiskStatus(request.getUserId());
            if (userRiskStatus != null && userRiskStatus.getStatus().equals(RiskConstants.UserRiskStatus.FREEZE)) {
                return buildFrozenResponse(request, userRiskStatus);
            }

            // 规则引擎执行
            List<RiskRule> triggeredRules = executeRuleEngine(request);

            // 策略引擎执行
            RiskCheckResponse response = executeStrategyEngine(request, triggeredRules, userRiskStatus);

            // 计算风险评分
            int riskScore = calculateRiskScore(request, triggeredRules, userRiskStatus);
            response.setRiskScore(riskScore);

            // 更新用户风险状态
            updateUserRiskStatus(request.getUserId(), riskScore, response.getRiskLevel());

            // 记录风控事件
            if (!response.getPass()) {
                recordRiskEvent(request, response, triggeredRules);
            }

            // 缓存响应
            cacheResponse(request.getCheckId(), response);

            long endTime = System.currentTimeMillis();
            log.debug("风控检查完成，耗时: {}ms，结果: {}", endTime - startTime, response.getPass());

            return response;

        } catch (Exception e) {
            log.error("风控检查失败，用户ID: {}, 事件类型: {}", request.getUserId(), request.getEventType(), e);
            return buildErrorResponse(request, e);
        }
    }

    @Override
    public void refreshRules() {
        try {
            log.info("开始刷新规则缓存...");
            loadRules();
            lastRefreshTime = LocalDateTime.now();
            log.info("规则缓存刷新完成");
        } catch (Exception e) {
            log.error("刷新规则缓存失败", e);
        }
    }

    @Override
    public void refreshStrategies() {
        try {
            log.info("开始刷新策略缓存...");
            loadStrategies();
            lastRefreshTime = LocalDateTime.now();
            log.info("策略缓存刷新完成");
        } catch (Exception e) {
            log.error("刷新策略缓存失败", e);
        }
    }

    @Override
    public EngineStatus getStatus() {
        return status;
    }

    /**
     * 加载规则
     */
    private void loadRules() {
        List<RiskRule> rules = riskRuleMapper.selectEnabledRulesByType(null);
        ruleCache.clear();
        for (RiskRule rule : rules) {
            ruleCache.put(rule.getRuleCode(), rule);
        }
        log.info("加载规则数量: {}", rules.size());
    }

    /**
     * 加载策略
     */
    private void loadStrategies() {
        List<RiskStrategy> strategies = riskStrategyMapper.selectList(null);
        strategyCache.clear();
        for (RiskStrategy strategy : strategies) {
            strategyCache.put(strategy.getStrategyCode(), strategy);
        }
        log.info("加载策略数量: {}", strategies.size());
    }

    /**
     * 加载白名单
     */
    private void loadWhitelist() {
        List<RiskWhitelist> whitelist = riskWhitelistMapper.selectList(null);
        whitelistCache.clear();
        for (RiskWhitelist item : whitelist) {
            whitelistCache.computeIfAbsent(getWhitelistKey(item.getWhitelistType()), k -> new HashSet<>())
                    .add(item);
        }
        log.info("加载白名单数量: {}", whitelist.size());
    }

    /**
     * 执行规则引擎
     */
    private List<RiskRule> executeRuleEngine(RiskCheckRequest request) {
        List<RiskRule> triggeredRules = new ArrayList<>();

        // 获取适用的规则
        List<RiskRule> applicableRules = getApplicableRules(request.getEventType());

        for (RiskRule rule : applicableRules) {
            if (evaluateRule(rule, request)) {
                triggeredRules.add(rule);
                log.debug("规则触发，规则编码: {}, 用户ID: {}", rule.getRuleCode(), request.getUserId());
            }
        }

        return triggeredRules;
    }

    /**
     * 执行策略引擎
     */
    private RiskCheckResponse executeStrategyEngine(RiskCheckRequest request, List<RiskRule> triggeredRules, UserRiskStatus userRiskStatus) {
        if (CollectionUtils.isEmpty(triggeredRules)) {
            return buildPassResponse(request);
        }

        // 获取适用的策略
        List<RiskStrategy> applicableStrategies = getApplicableStrategies(request.getEventType());

        // 按优先级排序
        applicableStrategies.sort(Comparator.comparingInt(RiskStrategy::getPriority));

        for (RiskStrategy strategy : applicableStrategies) {
            if (evaluateStrategy(strategy, triggeredRules, request)) {
                return buildRiskResponse(request, strategy, triggeredRules);
            }
        }

        // 默认响应
        return buildDefaultRiskResponse(request, triggeredRules);
    }

    /**
     * 获取适用的规则
     */
    private List<RiskRule> getApplicableRules(Integer eventType) {
        List<RiskRule> rules = new ArrayList<>();

        for (RiskRule rule : ruleCache.values()) {
            if (rule.getStatus().equals(RiskConstants.Status.ENABLED)) {
                // 根据事件类型过滤规则
                if (eventType == null || isRuleApplicableForEvent(rule, eventType)) {
                    rules.add(rule);
                }
            }
        }

        return rules;
    }

    /**
     * 获取适用的策略
     */
    private List<RiskStrategy> getApplicableStrategies(Integer eventType) {
        List<RiskStrategy> strategies = new ArrayList<>();

        for (RiskStrategy strategy : strategyCache.values()) {
            if (strategy.getStatus().equals(RiskConstants.Status.ENABLED)) {
                // 根据事件类型过滤策略
                if (eventType == null || isStrategyApplicableForEvent(strategy, eventType)) {
                    strategies.add(strategy);
                }
            }
        }

        return strategies;
    }

    /**
     * 评估规则
     */
    private boolean evaluateRule(RiskRule rule, RiskCheckRequest request) {
        try {
            // 解析规则表达式
            JSONObject ruleExpression = JSON.parseObject(rule.getRuleExpression());
            String condition = ruleExpression.getString("condition");
            BigDecimal threshold = rule.getThresholdValue();

            // 根据条件类型进行评估
            switch (condition) {
                case "AMOUNT_GT":
                    return evaluateAmountCondition(request, threshold, ">");
                case "AMOUNT_LT":
                    return evaluateAmountCondition(request, threshold, "<");
                case "FREQUENCY_GT":
                    return evaluateFrequencyCondition(request, threshold, ">");
                case "IP_BLACKLIST":
                    return evaluateIpCondition(request);
                case "TIME_RANGE":
                    return evaluateTimeCondition(request, ruleExpression);
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("规则评估失败，规则编码: {}", rule.getRuleCode(), e);
            return false;
        }
    }

    /**
     * 评估策略
     */
    private boolean evaluateStrategy(RiskStrategy strategy, List<RiskRule> triggeredRules, RiskCheckRequest request) {
        try {
            List<Long> ruleIds = JSON.parseArray(strategy.getRuleIds(), Long.class);

            switch (strategy.getMatchType()) {
                case RiskConstants.MatchType.ANY:
                    return triggeredRules.stream().anyMatch(rule -> ruleIds.contains(rule.getId()));
                case RiskConstants.MatchType.ALL:
                    return triggeredRules.stream().allMatch(rule -> ruleIds.contains(rule.getId()));
                case RiskConstants.MatchType.WEIGHTED:
                    return evaluateWeightedMatch(strategy, triggeredRules);
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("策略评估失败，策略编码: {}", strategy.getStrategyCode(), e);
            return false;
        }
    }

    /**
     * 计算风险评分
     */
    private int calculateRiskScore(RiskCheckRequest request, List<RiskRule> triggeredRules, UserRiskStatus userRiskStatus) {
        return riskScoreCalculator.calculate(request, triggeredRules, userRiskStatus);
    }

    /**
     * 白名单检查
     */
    private boolean checkWhitelist(RiskCheckRequest request) {
        // 检查用户白名单
        if (checkWhitelistByType(RiskConstants.WhitelistType.USER, String.valueOf(request.getUserId()))) {
            log.debug("用户在白名单中，用户ID: {}", request.getUserId());
            return true;
        }

        // 检查IP白名单
        if (request.getIpAddress() != null && checkWhitelistByType(RiskConstants.WhitelistType.IP, request.getIpAddress())) {
            log.debug("IP在白名单中，IP: {}", request.getIpAddress());
            return true;
        }

        // 检查设备白名单
        if (request.getDeviceInfo() != null && checkWhitelistByType(RiskConstants.WhitelistType.DEVICE, request.getDeviceInfo())) {
            log.debug("设备在白名单中，设备信息: {}", request.getDeviceInfo());
            return true;
        }

        return false;
    }

    /**
     * 检查指定类型的白名单
     */
    private boolean checkWhitelistByType(Integer type, String value) {
        Set<RiskWhitelist> whitelist = whitelistCache.get(getWhitelistKey(type));
        if (CollectionUtils.isEmpty(whitelist)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        for (RiskWhitelist item : whitelist) {
            if (item.getStatus().equals(RiskConstants.Status.ENABLED) &&
                item.getWhitelistValue().equals(value) &&
                now.isAfter(item.getEffectiveTime()) &&
                (item.getExpireTime() == null || now.isBefore(item.getExpireTime()))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 记录用户行为
     */
    private void logUserBehavior(RiskCheckRequest request) {
        try {
            UserBehaviorLog behaviorLog = new UserBehaviorLog();
            behaviorLog.setUserId(request.getUserId());
            behaviorLog.setBehaviorType(request.getEventType());
            behaviorLog.setAction(request.getBusinessType());
            behaviorLog.setIpAddress(request.getIpAddress());
            behaviorLog.setDeviceInfo(request.getDeviceInfo());
            behaviorLog.setUserAgent(request.getUserAgent());
            behaviorLog.setRequestUrl(request.getRequestUrl());
            behaviorLog.setRequestParams(JsonUtils.toJson(request.getRequestParams()));

            userBehaviorLogMapper.insert(behaviorLog);
        } catch (Exception e) {
            log.error("记录用户行为失败，用户ID: {}", request.getUserId(), e);
        }
    }

    /**
     * 记录风控事件
     */
    private void recordRiskEvent(RiskCheckRequest request, RiskCheckResponse response, List<RiskRule> triggeredRules) {
        try {
            if (CollectionUtils.isEmpty(triggeredRules)) {
                return;
            }

            RiskRule primaryRule = triggeredRules.get(0);

            RiskEvent event = new RiskEvent();
            event.setEventNo(IdGenerator.generateEventNo());
            event.setUserId(request.getUserId());
            event.setRuleId(primaryRule.getId());
            event.setRuleCode(primaryRule.getRuleCode());
            event.setEventType(request.getEventType());
            event.setRiskLevel(response.getRiskLevel());
            event.setEventData(JsonUtils.toJson(request));
            event.setActionType(response.getActionType());
            event.setStatus(RiskConstants.Status.PENDING);

            riskEventMapper.insert(event);
        } catch (Exception e) {
            log.error("记录风控事件失败，用户ID: {}", request.getUserId(), e);
        }
    }

    // Helper methods...
    private String getWhitelistKey(Integer type) {
        return "whitelist:" + type;
    }

    private boolean isRuleApplicableForEvent(RiskRule rule, Integer eventType) {
        // 简化实现，实际应根据规则配置判断
        return true;
    }

    private boolean isStrategyApplicableForEvent(RiskStrategy strategy, Integer eventType) {
        // 简化实现，实际应根据策略配置判断
        return true;
    }

    private boolean evaluateAmountCondition(RiskCheckRequest request, BigDecimal threshold, String operator) {
        // 简化实现，实际应根据业务逻辑评估
        return false;
    }

    private boolean evaluateFrequencyCondition(RiskCheckRequest request, BigDecimal threshold, String operator) {
        // 简化实现，实际应根据业务逻辑评估
        return false;
    }

    private boolean evaluateIpCondition(RiskCheckRequest request) {
        // 简化实现，实际应根据IP黑名单评估
        return false;
    }

    private boolean evaluateTimeCondition(RiskCheckRequest request, JSONObject ruleExpression) {
        // 简化实现，实际应根据时间范围评估
        return false;
    }

    private boolean evaluateWeightedMatch(RiskStrategy strategy, List<RiskRule> triggeredRules) {
        // 简化实现，实际应根据权重计算匹配度
        return false;
    }

    private RiskCheckResponse buildPassResponse(RiskCheckRequest request) {
        RiskCheckResponse response = new RiskCheckResponse();
        response.setCheckId(request.getCheckId());
        response.setUserId(request.getUserId());
        response.setRiskScore(0);
        response.setRiskLevel(RiskConstants.RiskLevel.LOW);
        response.setRiskLevelDesc("低风险");
        response.setPass(true);
        response.setActionType(RiskConstants.ActionType.NONE);
        response.setActionTypeDesc("无动作");
        response.setProcessTimestamp(System.currentTimeMillis());
        return response;
    }

    private RiskCheckResponse buildRiskResponse(RiskCheckRequest request, RiskStrategy strategy, List<RiskRule> triggeredRules) {
        RiskCheckResponse response = new RiskCheckResponse();
        response.setCheckId(request.getCheckId());
        response.setUserId(request.getUserId());
        response.setRiskLevel(strategy.getRiskLevel());
        response.setRiskLevelDesc(getRiskLevelDesc(strategy.getRiskLevel()));
        response.setPass(false);
        response.setActionType(strategy.getActionType());
        response.setActionTypeDesc(getActionTypeDesc(strategy.getActionType()));
        response.setProcessTimestamp(System.currentTimeMillis());

        // 设置触发的规则
        List<RiskCheckResponse.TriggeredRule> rules = new ArrayList<>();
        for (RiskRule rule : triggeredRules) {
            RiskCheckResponse.TriggeredRule triggeredRule = new RiskCheckResponse.TriggeredRule();
            triggeredRule.setRuleId(rule.getId());
            triggeredRule.setRuleCode(rule.getRuleCode());
            triggeredRule.setRuleName(rule.getRuleName());
            triggeredRule.setRuleType(rule.getRuleType());
            triggeredRule.setRiskLevel(rule.getRiskLevel());
            triggeredRule.setActionType(rule.getActionType());
            rules.add(triggeredRule);
        }
        response.setTriggeredRules(rules);

        return response;
    }

    private RiskCheckResponse buildDefaultRiskResponse(RiskCheckRequest request, List<RiskRule> triggeredRules) {
        RiskCheckResponse response = new RiskCheckResponse();
        response.setCheckId(request.getCheckId());
        response.setUserId(request.getUserId());
        response.setRiskLevel(RiskConstants.RiskLevel.MEDIUM);
        response.setRiskLevelDesc("中风险");
        response.setPass(false);
        response.setActionType(RiskConstants.ActionType.WARNING);
        response.setActionTypeDesc("警告");
        response.setProcessTimestamp(System.currentTimeMillis());
        return response;
    }

    private RiskCheckResponse buildFrozenResponse(RiskCheckRequest request, UserRiskStatus userRiskStatus) {
        RiskCheckResponse response = new RiskCheckResponse();
        response.setCheckId(request.getCheckId());
        response.setUserId(request.getUserId());
        response.setRiskScore(userRiskStatus.getRiskScore());
        response.setRiskLevel(userRiskStatus.getRiskLevel());
        response.setRiskLevelDesc(getRiskLevelDesc(userRiskStatus.getRiskLevel()));
        response.setPass(false);
        response.setActionType(RiskConstants.ActionType.FREEZE);
        response.setActionTypeDesc("账户冻结");
        response.setRejectReason(userRiskStatus.getFreezeReason());
        response.setProcessTimestamp(System.currentTimeMillis());
        return response;
    }

    private RiskCheckResponse buildErrorResponse(RiskCheckRequest request, Exception e) {
        RiskCheckResponse response = new RiskCheckResponse();
        response.setCheckId(request.getCheckId());
        response.setUserId(request.getUserId());
        response.setRiskLevel(RiskConstants.RiskLevel.SEVERE);
        response.setRiskLevelDesc("严重风险");
        response.setPass(false);
        response.setActionType(RiskConstants.ActionType.REJECT);
        response.setActionTypeDesc("系统错误");
        response.setRejectReason("风控系统异常: " + e.getMessage());
        response.setProcessTimestamp(System.currentTimeMillis());
        return response;
    }

    private UserRiskStatus getUserRiskStatus(Long userId) {
        String cacheKey = RiskConstants.USER_RISK_STATUS_KEY + userId;
        UserRiskStatus status = (UserRiskStatus) redisTemplate.opsForValue().get(cacheKey);
        if (status == null) {
            status = userRiskStatusMapper.selectById(userId);
            if (status != null) {
                redisTemplate.opsForValue().set(cacheKey, status, 10, TimeUnit.MINUTES);
            }
        }
        return status;
    }

    private void updateUserRiskStatus(Long userId, Integer riskScore, Integer riskLevel) {
        // 简化实现，实际应更新用户风险状态
    }

    private void cacheResponse(String checkId, RiskCheckResponse response) {
        if (checkId != null) {
            String cacheKey = RiskConstants.IDEMPOTENT_PREFIX + checkId;
            redisTemplate.opsForValue().set(cacheKey, response, 1, TimeUnit.HOURS);
        }
    }

    private String getRiskLevelDesc(Integer riskLevel) {
        switch (riskLevel) {
            case RiskConstants.RiskLevel.LOW: return "低风险";
            case RiskConstants.RiskLevel.MEDIUM: return "中风险";
            case RiskConstants.RiskLevel.HIGH: return "高风险";
            case RiskConstants.RiskLevel.SEVERE: return "严重风险";
            default: return "未知";
        }
    }

    private String getActionTypeDesc(Integer actionType) {
        switch (actionType) {
            case RiskConstants.ActionType.WARNING: return "警告";
            case RiskConstants.ActionType.LIMIT: return "限制";
            case RiskConstants.ActionType.FREEZE: return "冻结";
            case RiskConstants.ActionType.REJECT: return "拒绝";
            case RiskConstants.ActionType.NONE: return "无动作";
            default: return "未知";
        }
    }

    @Autowired
    private com.ppcex.risk.mapper.RiskEventMapper riskEventMapper;
}