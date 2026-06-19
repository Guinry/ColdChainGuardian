#pragma once

#if __has_include("Config.local.h")
#include "Config.local.h"
#endif

#ifndef CCG_WIFI_SSID
#define CCG_WIFI_SSID "your-wifi-ssid"
#endif

#ifndef CCG_WIFI_PASSWORD
#define CCG_WIFI_PASSWORD "your-wifi-password"
#endif

#ifndef CCG_API_BASE_URL
#define CCG_API_BASE_URL "https://coldchain.guinry.cn"
#endif

#ifndef CCG_API_TELEMETRY_PATH
#define CCG_API_TELEMETRY_PATH "/api/iot/telemetry"
#endif

#ifndef CCG_API_AUTH_TOKEN
#define CCG_API_AUTH_TOKEN ""
#endif

#ifndef CCG_DEVICE_CODE
#define CCG_DEVICE_CODE "TH-A01-001"
#endif

#ifndef CCG_ZONE_CODE
#define CCG_ZONE_CODE "A01"
#endif

#ifndef CCG_SAMPLE_INTERVAL_MS
#define CCG_SAMPLE_INTERVAL_MS 10000UL
#endif

#ifndef CCG_NETWORK_CHECK_INTERVAL_MS
#define CCG_NETWORK_CHECK_INTERVAL_MS 5000UL
#endif

#ifndef CCG_WIFI_CONNECT_TIMEOUT_MS
#define CCG_WIFI_CONNECT_TIMEOUT_MS 20000UL
#endif

#ifndef CCG_NTP_SERVER_1
#define CCG_NTP_SERVER_1 "ntp.aliyun.com"
#endif

#ifndef CCG_NTP_SERVER_2
#define CCG_NTP_SERVER_2 "pool.ntp.org"
#endif

#ifndef CCG_GMT_OFFSET_SEC
#define CCG_GMT_OFFSET_SEC 28800L
#endif

#ifndef CCG_DAYLIGHT_OFFSET_SEC
#define CCG_DAYLIGHT_OFFSET_SEC 0L
#endif

#ifndef CCG_OLED_ENABLED
#define CCG_OLED_ENABLED 1
#endif

#ifndef CCG_OLED_I2C_ADDRESS
#define CCG_OLED_I2C_ADDRESS 0x3C
#endif

#ifndef CCG_OLED_WIDTH
#define CCG_OLED_WIDTH 128
#endif

#ifndef CCG_OLED_HEIGHT
#define CCG_OLED_HEIGHT 64
#endif

#ifndef CCG_OLED_RESET_PIN
#define CCG_OLED_RESET_PIN -1
#endif
