package com.coldchain.guardian.common.constant;

public class Constants {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String SYSTEM_USER = "SYSTEM";

    public static final String TELEMETRY_TOPIC = "telemetry/data";
    public static final String ALERT_TOPIC = "alerts/data";

    public static final String SESSION_USER_KEY = "CURRENT_USER";

    // 告警级别常量
    public static final Integer ALERT_LEVEL_LOW = 1;
    public static final Integer ALERT_LEVEL_MEDIUM = 2;
    public static final Integer ALERT_LEVEL_HIGH = 3;

    private Constants() {
        // Prevent instantiation
    }
}