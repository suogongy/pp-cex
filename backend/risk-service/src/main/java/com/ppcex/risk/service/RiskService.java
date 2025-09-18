package com.ppcex.risk.service;

import com.ppcex.risk.dto.RiskCheckRequest;
import com.ppcex.risk.dto.RiskCheckResponse;
import com.ppcex.risk.dto.RiskEventQuery;
import com.ppcex.risk.dto.RiskStatisticsDTO;

import java.util.Map;

/**
 * 风控服务接口
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface RiskService {

    /**
     * 实时风控检查
     *
     * @param request 风控检查请求
     * @return 风控检查响应
     */
    RiskCheckResponse checkRisk(RiskCheckRequest request);

    /**
     * 批量风控检查
     *
     * @param requests 风控检查请求列表
     * @return 风控检查响应列表
     */
    Map<String, RiskCheckResponse> batchCheckRisk(Map<String, RiskCheckRequest> requests);

    /**
     * 计算用户风险评分
     *
     * @param userId 用户ID
     * @param context 上下文信息
     * @return 风险评分
     */
    int calculateUserRiskScore(Long userId, Map<String, Object> context);

    /**
     * 更新用户风险状态
     *
     * @param userId 用户ID
     * @param riskScore 风险评分
     * @param riskLevel 风险等级
     */
    void updateUserRiskStatus(Long userId, Integer riskScore, Integer riskLevel);

    /**
     * 执行风控动作
     *
     * @param userId 用户ID
     * @param actionType 动作类型
     * @param actionParams 动作参数
     * @return 执行结果
     */
    boolean executeRiskAction(Long userId, Integer actionType, Map<String, Object> actionParams);

    /**
     * 检查白名单
     *
     * @param type 白名单类型
     * @param value 白名单值
     * @return 是否在白名单中
     */
    boolean checkWhitelist(Integer type, String value);

    /**
     * 查询风控事件
     *
     * @param query 查询条件
     * @return 风控事件列表
     */
    Object queryRiskEvents(RiskEventQuery query);

    /**
     * 处理风控事件
     *
     * @param eventId 事件ID
     * @param processor 处理人
     * @param remark 备注
     * @return 处理结果
     */
    boolean processRiskEvent(Long eventId, String processor, String remark);

    /**
     * 获取风控统计数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 风控统计数据
     */
    RiskStatisticsDTO getRiskStatistics(String startDate, String endDate);

    /**
     * 记录用户行为
     *
     * @param userId 用户ID
     * @param behaviorType 行为类型
     * @param action 操作动作
     * @param context 上下文信息
     */
    void logUserBehavior(Long userId, Integer behaviorType, String action, Map<String, Object> context);
}