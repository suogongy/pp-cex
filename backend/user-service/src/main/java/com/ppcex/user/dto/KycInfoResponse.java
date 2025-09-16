package com.ppcex.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "KYC认证信息响应")
public class KycInfoResponse {

    @Schema(description = "KYC ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "证件类型")
    private String idCardType;

    @Schema(description = "证件号码")
    private String idCardNo;

    @Schema(description = "身份证正面照片URL")
    private String idCardFront;

    @Schema(description = "身份证背面照片URL")
    private String idCardBack;

    @Schema(description = "手持身份证照片URL")
    private String idCardHand;

    @Schema(description = "国籍")
    private String nationality;

    @Schema(description = "出生日期")
    private String birthday;

    @Schema(description = "性别 1-男 2-女")
    private Integer gender;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "职业")
    private String occupation;

    @Schema(description = "交易目的")
    private String purpose;

    @Schema(description = "状态 0-待审核 1-已通过 2-已拒绝")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDescription;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核人")
    private String auditUser;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // 状态描述
    public String getStatusDescription() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            default:
                return "未知";
        }
    }
}