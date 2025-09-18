package com.ppcex.risk.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 风控检查请求DTO
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class RiskCheckRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 事件类型
     */
    @NotNull(message = "事件类型不能为空")
    private Integer eventType;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 事件数据
     */
    private Map<String, Object> eventData;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private Map<String, Object> requestParams;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 请求时间戳
     */
    private Long requestTimestamp;

    /**
     * 风控检查ID（用于幂等性）
     */
    private String checkId;
}