package com.ppcex.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("risk_control")
public class RiskControl {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("rule_name")
    private String ruleName;

    @TableField("rule_type")
    private Integer ruleType;

    @TableField("rule_code")
    private String ruleCode;

    @TableField("description")
    private String description;

    @TableField("threshold_value")
    private BigDecimal thresholdValue;

    @TableField("threshold_type")
    private String thresholdType;

    @TableField("action_type")
    private Integer actionType;

    @TableField("risk_level")
    private Integer riskLevel;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("priority")
    private Integer priority;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    public enum RuleType {
        IP_BLACKLIST(1, "IP黑名单"),
        RATE_LIMIT(2, "频率限制"),
        AMOUNT_LIMIT(3, "金额限制"),
        BEHAVIOR_PATTERN(4, "行为模式"),
        LOCATION_RISK(5, "地理位置风险"),
        DEVICE_RISK(6, "设备风险"),
        TIME_RISK(7, "时间风险");

        private final Integer code;
        private final String desc;

        RuleType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum ActionType {
        ALERT(1, "告警"),
        FREEZE(2, "冻结"),
        REJECT(3, "拒绝"),
        MANUAL_REVIEW(4, "人工审核"),
        LIMIT_ACCESS(5, "限制访问");

        private final Integer code;
        private final String desc;

        ActionType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum RiskLevel {
        LOW(1, "低"),
        MEDIUM(2, "中"),
        HIGH(3, "高"),
        CRITICAL(4, "严重");

        private final Integer code;
        private final String desc;

        RiskLevel(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}