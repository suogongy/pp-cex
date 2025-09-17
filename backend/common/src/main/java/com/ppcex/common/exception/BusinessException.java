package com.ppcex.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误数据
     */
    private Object data;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Object data, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 用户不存在
     */
    public static BusinessException userNotFound() {
        return new BusinessException(1001, "用户不存在");
    }

    /**
     * 密码错误
     */
    public static BusinessException passwordError() {
        return new BusinessException(1002, "密码错误");
    }

    /**
     * 用户已被禁用
     */
    public static BusinessException userDisabled() {
        return new BusinessException(1003, "用户已被禁用");
    }

    /**
     * 用户已存在
     */
    public static BusinessException userExists() {
        return new BusinessException(1004, "用户已存在");
    }

    /**
     * 验证码错误
     */
    public static BusinessException verifyCodeError() {
        return new BusinessException(1005, "验证码错误");
    }

    /**
     * 订单不存在
     */
    public static BusinessException orderNotFound() {
        return new BusinessException(2001, "订单不存在");
    }

    /**
     * 订单状态错误
     */
    public static BusinessException orderStatusError() {
        return new BusinessException(2002, "订单状态错误");
    }

    /**
     * 余额不足
     */
    public static BusinessException insufficientBalance() {
        return new BusinessException(2003, "余额不足");
    }

    /**
     * 超出交易限制
     */
    public static BusinessException tradingLimitExceeded() {
        return new BusinessException(2004, "超出交易限制");
    }

    /**
     * 钱包地址错误
     */
    public static BusinessException walletAddressError() {
        return new BusinessException(3001, "钱包地址错误");
    }

    /**
     * 提现金额不足
     */
    public static BusinessException insufficientWithdrawAmount() {
        return new BusinessException(3002, "提现金额不足");
    }

    /**
     * 提现审核中
     */
    public static BusinessException withdrawInAudit() {
        return new BusinessException(3003, "提现审核中");
    }

    /**
     * 风控规则触发
     */
    public static BusinessException riskControlTriggered() {
        return new BusinessException(4001, "风控规则触发");
    }

    /**
     * 参数错误
     */
    public static BusinessException paramError(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 未授权
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /**
     * 禁止访问
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 资源不存在
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 请求过于频繁
     */
    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(429, message);
    }

    /**
     * 系统错误
     */
    public static BusinessException systemError(String message) {
        return new BusinessException(500, message);
    }
}