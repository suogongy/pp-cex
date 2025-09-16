package com.cex.common.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 通用响应结果
 *
 * @param <T> 数据类型
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 追踪ID
     */
    private String traceId;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success");
    }

    /**
     * 成功响应带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应带消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "error");
    }

    /**
     * 失败响应带消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message);
    }

    /**
     * 失败响应带错误码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败响应带错误码、消息和数据
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> paramError(String message) {
        return new Result<>(400, message);
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message);
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message);
    }

    /**
     * 请求过于频繁
     */
    public static <T> Result<T> tooManyRequests(String message) {
        return new Result<>(429, message);
    }

    /**
     * 业务错误
     */
    public static <T> Result<T> businessError(String message) {
        return new Result<>(1000, message);
    }

    /**
     * 用户不存在
     */
    public static <T> Result<T> userNotFound() {
        return new Result<>(1001, "用户不存在");
    }

    /**
     * 密码错误
     */
    public static <T> Result<T> passwordError() {
        return new Result<>(1002, "密码错误");
    }

    /**
     * 用户已被禁用
     */
    public static <T> Result<T> userDisabled() {
        return new Result<>(1003, "用户已被禁用");
    }

    /**
     * 用户已存在
     */
    public static <T> Result<T> userExists() {
        return new Result<>(1004, "用户已存在");
    }

    /**
     * 验证码错误
     */
    public static <T> Result<T> verifyCodeError() {
        return new Result<>(1005, "验证码错误");
    }

    /**
     * 订单不存在
     */
    public static <T> Result<T> orderNotFound() {
        return new Result<>(2001, "订单不存在");
    }

    /**
     * 订单状态错误
     */
    public static <T> Result<T> orderStatusError() {
        return new Result<>(2002, "订单状态错误");
    }

    /**
     * 余额不足
     */
    public static <T> Result<T> insufficientBalance() {
        return new Result<>(2003, "余额不足");
    }

    /**
     * 超出交易限制
     */
    public static <T> Result<T> tradingLimitExceeded() {
        return new Result<>(2004, "超出交易限制");
    }

    /**
     * 钱包地址错误
     */
    public static <T> Result<T> walletAddressError() {
        return new Result<>(3001, "钱包地址错误");
    }

    /**
     * 提现金额不足
     */
    public static <T> Result<T> insufficientWithdrawAmount() {
        return new Result<>(3002, "提现金额不足");
    }

    /**
     * 提现审核中
     */
    public static <T> Result<T> withdrawInAudit() {
        return new Result<>(3003, "提现审核中");
    }

    /**
     * 风控规则触发
     */
    public static <T> Result<T> riskControlTriggered() {
        return new Result<>(4001, "风控规则触发");
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}