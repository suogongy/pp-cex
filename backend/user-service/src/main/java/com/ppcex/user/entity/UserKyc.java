package com.ppcex.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户KYC表")
@TableName("user_kyc")
public class UserKyc implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "KYC ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "证件类型")
    @TableField("id_card_type")
    private String idCardType;

    @Schema(description = "证件号码")
    @TableField("id_card_no")
    private String idCardNo;

    @Schema(description = "身份证正面照片")
    @TableField("id_card_front")
    private String idCardFront;

    @Schema(description = "身份证背面照片")
    @TableField("id_card_back")
    private String idCardBack;

    @Schema(description = "手持身份证照片")
    @TableField("id_card_hand")
    private String idCardHand;

    @Schema(description = "国籍")
    @TableField("nationality")
    private String nationality;

    @Schema(description = "出生日期")
    @TableField("birthday")
    private LocalDate birthday;

    @Schema(description = "性别 1-男 2-女")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "地址")
    @TableField("address")
    private String address;

    @Schema(description = "职业")
    @TableField("occupation")
    private String occupation;

    @Schema(description = "交易目的")
    @TableField("purpose")
    private String purpose;

    @Schema(description = "状态 0-待审核 1-已通过 2-已拒绝")
    @TableField("status")
    private Integer status;

    @Schema(description = "审核时间")
    @TableField("audit_time")
    private LocalDateTime auditTime;

    @Schema(description = "审核人")
    @TableField("audit_user")
    private String auditUser;

    @Schema(description = "拒绝原因")
    @TableField("reject_reason")
    private String rejectReason;

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