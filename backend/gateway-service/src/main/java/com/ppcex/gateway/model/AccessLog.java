package com.ppcex.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 访问日志模型
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog {

    /** 日志ID */
    private String id;

    /** 请求ID */
    private String requestId;

    /** 追踪ID */
    private String traceId;

    /** 用户ID */
    private String userId;

    /** 客户端IP */
    private String clientIp;

    /** 请求方法 */
    private String method;

    /** 请求路径 */
    private String path;

    /** 请求协议 */
    private String protocol;

    /** 请求头 */
    private Map<String, String> headers;

    /** 请求参数 */
    private Map<String, String> parameters;

    /** 请求体（摘要） */
    private String requestBody;

    /** 响应状态码 */
    private Integer responseStatus;

    /** 响应耗时（毫秒） */
    private Long duration;

    /** 响应大小（字节） */
    private Long responseSize;

    /** 用户代理 */
    private String userAgent;

    /** 请求时间 */
    private LocalDateTime requestTime;

    /** 响应时间 */
    private LocalDateTime responseTime;

    /** 服务名称 */
    private String serviceName;

    /** 错误信息 */
    private String errorMessage;

    /** 创建时间 */
    private LocalDateTime createTime;
}