package com.ppcex.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("financial_daily_report")
public class FinancialDailyReport {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("report_date")
    private LocalDate reportDate;

    @TableField("total_users")
    private Long totalUsers;

    @TableField("active_users")
    private Long activeUsers;

    @TableField("new_users")
    private Long newUsers;

    @TableField("total_trades")
    private Long totalTrades;

    @TableField("total_trade_volume")
    private BigDecimal totalTradeVolume;

    @TableField("total_fee_income")
    private BigDecimal totalFeeIncome;

    @TableField("total_recharge")
    private BigDecimal totalRecharge;

    @TableField("total_withdraw")
    private BigDecimal totalWithdraw;

    @TableField("net_deposit")
    private BigDecimal netDeposit;

    @TableField("total_assets")
    private BigDecimal totalAssets;

    @TableField("hot_wallet_balance")
    private BigDecimal hotWalletBalance;

    @TableField("cold_wallet_balance")
    private BigDecimal coldWalletBalance;

    @TableField("risk_events_count")
    private Integer riskEventsCount;

    @TableField("report_status")
    private Integer reportStatus;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    public enum ReportStatus {
        DRAFT(1, "草稿"),
        CONFIRMED(2, "已确认"),
        AUDITED(3, "已审核"),
        PUBLISHED(4, "已发布");

        private final Integer code;
        private final String desc;

        ReportStatus(Integer code, String desc) {
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