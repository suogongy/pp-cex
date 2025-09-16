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
@TableName("risk_event")
public class RiskEvent {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("event_no")
    private String eventNo;

    @TableField("user_id")
    private Long userId;

    @TableField("event_type")
    private Integer eventType;

    @TableField("risk_level")
    private Integer riskLevel;

    @TableField("event_content")
    private String eventContent;

    @TableField("related_data")
    private String relatedData;

    @TableField("client_ip")
    private String clientIp;

    @TableField("device_info")
    private String deviceInfo;

    @TableField("location")
    private String location;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("coin_id")
    private String coinId;

    @TableField("rule_triggered")
    private String ruleTriggered;

    @TableField("action_taken")
    private String actionTaken;

    @TableField("status")
    private Integer status;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("handle_user")
    private String handleUser;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    public enum EventType {
        LOGIN_ABNORMAL(1, "登录异常"),
        TRADING_ABNORMAL(2, "交易异常"),
        WITHDRAW_ABNORMAL(3, "提现异常"),
        DEPOSIT_ABNORMAL(4, "充值异常"),
        BEHAVIOR_ABNORMAL(5, "行为异常"),
        DEVICE_ABNORMAL(6, "设备异常"),
        IP_ABNORMAL(7, "IP异常"),
        AMOUNT_ABNORMAL(8, "金额异常");

        private final Integer code;
        private final String desc;

        EventType(Integer code, String desc) {
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

    public enum Status {
        PENDING(1, "待处理"),
        PROCESSING(2, "处理中"),
        RESOLVED(3, "已解决"),
        IGNORED(4, "已忽略"),
        ESCALATED(5, "已升级");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
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