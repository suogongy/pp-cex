package com.ppcex.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户资产表")
@TableName("user_asset")
public class UserAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "资产ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "币种ID")
    @TableField("coin_id")
    private String coinId;

    @Schema(description = "币种名称")
    @TableField("coin_name")
    private String coinName;

    @Schema(description = "可用余额")
    @TableField("available_balance")
    private BigDecimal availableBalance;

    @Schema(description = "冻结余额")
    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    @Schema(description = "总余额")
    @TableField("total_balance")
    private BigDecimal totalBalance;

    @Schema(description = "充值地址")
    @TableField("address")
    private String address;

    @Schema(description = "状态 1-正常 2-冻结")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除标记 0-未删除 1-已删除")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}