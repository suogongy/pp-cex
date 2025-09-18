package com.ppcex.notify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 通知请求DTO
 */
@Data
public class NotifyRequestDTO {

    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private Integer businessType;

    /**
     * 通知类型
     */
    @NotNull(message = "通知类型不能为空")
    private Integer notifyType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 接收者
     */
    @NotBlank(message = "接收者不能为空")
    private String recipient;

    /**
     * 模板编码
     */
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 模板变量
     */
    private Map<String, Object> templateVars;

    /**
     * 语言
     */
    private String language = "zh-CN";

    /**
     * 优先级
     */
    private Integer priority = 1;
}