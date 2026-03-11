package com.coldchain.guardian.common.exception;

public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统错误"),
    PARAMETER_ERROR(400, "参数错误"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    REQUEST_TIMEOUT(408, "请求超时"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 业务错误码
    USER_NOT_FOUND(10001, "用户不存在"),
    INVALID_CREDENTIALS(10002, "无效的凭证"),
    USERNAME_EXISTS(10003, "用户名已存在"),
    DEVICE_NOT_EXISTS(10004, "设备不存在"),
    AREA_NOT_EXISTS(10005, "库区不存在"),
    WORK_ORDER_NOT_EXISTS(10006, "工单不存在"),
    ALERT_NOT_EXISTS(10007, "告警不存在"),
    ACCOUNT_DISABLED(10008, "账户已被禁用"),
    TOKEN_GENERATION_FAILED(10009, "令牌生成失败，请联系管理员"),
    AREA_NAME_EXISTS(10010, "库区名称已存在"),
    AREA_CODE_EXISTS(10011, "库区编码已存在"),
    AREA_HAS_CHILDREN(10012, "库区存在子库区，无法删除"),
    AUTH_FAILED(10013, "认证失败"),
    BUSINESS_ERROR(10014, "业务错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}