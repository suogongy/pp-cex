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
@TableName("financial_flow")
public class FinancialFlow {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("flow_no")
    private String flowNo;

    @TableField("user_id")
    private Long userId;

    @TableField("coin_id")
    private String coinId;

    @TableField("coin_name")
    private String coinName;

    @TableField("business_type")
    private Integer businessType;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("balance_before")
    private BigDecimal balanceBefore;

    @TableField("balance_after")
    private BigDecimal balanceAfter;

    @TableField("fee")
    private BigDecimal fee;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    @TableField("ref_order_no")
    private String refOrderNo;

    @TableField("ref_tx_hash")
    private String refTxHash;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    public enum BusinessType {
        RECHARGE(1, "充值"),
        WITHDRAW(2, "提现"),
        BUY(3, "买入"),
        SELL(4, "卖出"),
        FEE(5, "手续费"),
        TRANSFER(6, "转账"),
        OTHER(99, "其他");

        private final Integer code;
        private final String desc;

        BusinessType(Integer code, String desc) {
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
        SUCCESS(1, "成功"),
        FAILED(2, "失败"),
        PENDING(3, "处理中");

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