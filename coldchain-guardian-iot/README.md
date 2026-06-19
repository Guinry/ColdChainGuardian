# coldchain-guardian-iot

ESP32 + SHT31 temperature and humidity acquisition node for ColdChain Guardian.

## Hardware

- ESP32-WROOM-32E development board
- SHT31 temperature and humidity module
- 0.96 inch SSD1306 OLED display, I2C, 4-pin (optional local display)
- Female-to-female jumper wires
- Type-C USB data cable
- 5V USB power adapter

## Wiring

| SHT31 | ESP32 | Description |
| --- | --- | --- |
| VCC | 3V3 | Sensor power, use 3.3V |
| GND | GND | Common ground |
| SDA | GPIO21 | I2C data |
| SCL | GPIO22 | I2C clock |

Do not power SHT31 with 5V when it is connected directly to ESP32 I2C pins. Many modules pull SDA/SCL up to VCC, and ESP32 GPIO is not 5V tolerant.

### SSD1306 OLED wiring

The firmware supports a 0.96 inch SSD1306 OLED display with I2C. OLED and SHT31 share the same I2C bus: `SDA -> GPIO21`, `SCL -> GPIO22`.

| OLED | ESP32 | Description |
| --- | --- | --- |
| VCC | 3V3 splitter | OLED power |
| GND | GND splitter | Common ground |
| SCL | GPIO22 splitter | I2C clock |
| SDA | GPIO21 splitter | I2C data |

Use four 1-to-2 female jumper wires:

| ESP32 | Split to |
| --- | --- |
| 3V3 | SHT31 VCC + OLED VCC |
| GND | SHT31 GND + OLED GND |
| GPIO21 | SHT31 SDA + OLED SDA |
| GPIO22 | SHT31 SCL + OLED SCL |

Do not power SHT31 or OLED from 5V when their SDA/SCL pins are connected directly to ESP32 GPIO. ESP32 GPIO is not 5V tolerant.

## Configure

Copy `include/Config.local.example.h` to `include/Config.local.h`, then edit Wi-Fi and backend settings:

```cpp
#define CCG_WIFI_SSID "your-wifi-ssid"
#define CCG_WIFI_PASSWORD "your-wifi-password"
#define CCG_API_BASE_URL "https://coldchain.guinry.cn"
#define CCG_DEVICE_CODE "TH-A01-001"
#define CCG_ZONE_CODE "A01"
#define CCG_OLED_ENABLED 1
#define CCG_OLED_I2C_ADDRESS 0x3C
```

Use the deployed backend domain for `CCG_API_BASE_URL`. Do not use `localhost`, because ESP32 is a separate device.

## Build and upload

In VS Code PlatformIO:

1. Open this folder.
2. Select `PlatformIO: Build`.
3. Select `PlatformIO: Upload`.
4. Open Serial Monitor with `115200` baud.

Expected serial output:

```text
[sensor] SHT31 ready at 0x44
[network] connected, ip=192.168.x.x
[sensor] temperature=24.60 C, humidity=58.20 %RH
[api] POST https://coldchain.guinry.cn/api/iot/telemetry
[display] SSD1306 OLED ready at 0x3C
[display] OLED shares SDA->GPIO21, SCL->GPIO22 with SHT31
```

The OLED always shows the latest local sensor reading. If Wi-Fi or backend upload fails, the screen still updates temperature and humidity and shows `NET:OFF`.

## Backend note

The firmware posts telemetry to `/api/iot/telemetry` by default. If the backend endpoint is changed later, update `CCG_API_TELEMETRY_PATH` in `Config.local.h`.
