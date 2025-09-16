package com.ppcex.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "KYC认证提交请求")
public class KycSubmitRequest {

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 100, message = "真实姓名长度必须在2-100个字符之间")
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank(message = "证件类型不能为空")
    @Schema(description = "证件类型", example = "身份证")
    private String idCardType;

    @NotBlank(message = "证件号码不能为空")
    @Size(min = 15, max = 50, message = "证件号码长度必须在15-50个字符之间")
    @Schema(description = "证件号码")
    private String idCardNo;

    @NotBlank(message = "国籍不能为空")
    @Schema(description = "国籍")
    private String nationality;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    @Schema(description = "出生日期")
    private LocalDate birthday;

    @NotNull(message = "性别不能为空")
    @Schema(description = "性别 1-男 2-女")
    private Integer gender;

    @NotBlank(message = "地址不能为空")
    @Size(min = 5, max = 255, message = "地址长度必须在5-255个字符之间")
    @Schema(description = "地址")
    private String address;

    @Size(max = 100, message = "职业长度不能超过100个字符")
    @Schema(description = "职业")
    private String occupation;

    @Size(max = 200, message = "交易目的长度不能超过200个字符")
    @Schema(description = "交易目的")
    private String purpose;

    @NotBlank(message = "身份证正面照片不能为空")
    @Schema(description = "身份证正面照片URL")
    private String idCardFront;

    @NotBlank(message = "身份证背面照片不能为空")
    @Schema(description = "身份证背面照片URL")
    private String idCardBack;

    @Size(max = 255, message = "手持身份证照片URL长度不能超过255个字符")
    @Schema(description = "手持身份证照片URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String idCardHand;
}