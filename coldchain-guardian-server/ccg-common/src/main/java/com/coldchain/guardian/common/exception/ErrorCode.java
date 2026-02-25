package com.coldchain.guardian.common.exception;

public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统错误"),
    PARAMETER_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    REQUEST_TIMEOUT(408, "请求超时"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 业务错误码
    USER_NOT_EXISTS(10001, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(10002, "用户名或密码错误"),
    DEVICE_NOT_EXISTS(10003, "设备不存在"),
    AREA_NOT_EXISTS(10004, "库区不存在"),
    WORK_ORDER_NOT_EXISTS(10005, "工单不存在"),
    ALERT_NOT_EXISTS(10006, "告警不存在");

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