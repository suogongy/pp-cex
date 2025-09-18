package com.ppcex.risk.engine;

import com.ppcex.risk.dto.RiskCheckRequest;
import com.ppcex.risk.dto.RiskCheckResponse;

/**
 * 风控引擎接口
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface RiskEngine {

    /**
     * 执行风控检查
     *
     * @param request 风控检查请求
     * @return 风控检查响应
     */
    RiskCheckResponse execute(RiskCheckRequest request);

    /**
     * 初始化风控引擎
     */
    void initialize();

    /**
     * 刷新规则缓存
     */
    void refreshRules();

    /**
     * 刷新策略缓存
     */
    void refreshStrategies();

    /**
     * 获取引擎状态
     *
     * @return 引擎状态
     */
    EngineStatus getStatus();

    /**
     * 引擎状态
     */
    enum EngineStatus {
        INITIALIZING,    // 初始化中
        RUNNING,         // 运行中
        STOPPED,         // 已停止
        ERROR           // 错误状态
    }
}