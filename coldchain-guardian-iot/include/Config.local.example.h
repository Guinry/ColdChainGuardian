#pragma once

#define CCG_WIFI_SSID "your-wifi-ssid"
#define CCG_WIFI_PASSWORD "your-wifi-password"

// Use the deployed backend domain for remote upload.
#define CCG_API_BASE_URL "https://coldchain.guinry.cn"
#define CCG_API_TELEMETRY_PATH "/api/iot/telemetry"
#define CCG_API_AUTH_TOKEN ""

#define CCG_DEVICE_CODE "TH-A01-001"
#define CCG_ZONE_CODE "A01"

#define CCG_SAMPLE_INTERVAL_MS 10000UL

// 0.96 inch SSD1306 OLED, I2C, 128x64. Common address: 0x3C.
#define CCG_OLED_ENABLED 1
#define CCG_OLED_I2C_ADDRESS 0x3C
