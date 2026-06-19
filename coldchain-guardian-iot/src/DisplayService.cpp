#include "DisplayService.h"

#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Wire.h>

#include "Config.h"

namespace {
Adafruit_SSD1306 display(CCG_OLED_WIDTH, CCG_OLED_HEIGHT, &Wire,
                         CCG_OLED_RESET_PIN);
uint8_t oledAddress = CCG_OLED_I2C_ADDRESS;

String networkLabel(bool connected, int rssi) {
  if (!connected) {
    return "NET:OFF";
  }
  if (rssi == 0) {
    return "NET:ON";
  }
  return "NET:" + String(rssi);
}
} // namespace

bool DisplayService::begin() {
#if CCG_OLED_ENABLED
  Serial.println("[display] init OLED on shared I2C bus SDA->GPIO21, SCL->GPIO22");
  Wire.setClock(100000);

  if (!display.begin(SSD1306_SWITCHCAPVCC, CCG_OLED_I2C_ADDRESS)) {
    const uint8_t fallbackAddress =
        CCG_OLED_I2C_ADDRESS == 0x3C ? 0x3D : 0x3C;
    Serial.print("[display] SSD1306 OLED not found at 0x");
    Serial.print(CCG_OLED_I2C_ADDRESS, HEX);
    Serial.print(", try 0x");
    Serial.println(fallbackAddress, HEX);

    if (!display.begin(SSD1306_SWITCHCAPVCC, fallbackAddress)) {
      ready_ = false;
      Serial.println("[display] SSD1306 OLED not found. Check VCC/GND/SDA/SCL.");
      Serial.println("[display] Expected OLED SDA->GPIO21, SCL->GPIO22");
      return false;
    }
    oledAddress = fallbackAddress;
  } else {
    oledAddress = CCG_OLED_I2C_ADDRESS;
  }

  display.clearDisplay();
  display.setTextColor(SSD1306_WHITE);
  display.setTextWrap(false);
  display.ssd1306_command(SSD1306_DISPLAYON);
  display.ssd1306_command(SSD1306_SETCONTRAST);
  display.ssd1306_command(0xFF);

  display.fillRect(0, 0, CCG_OLED_WIDTH, 16, SSD1306_WHITE);
  display.setTextColor(SSD1306_BLACK);
  display.setTextSize(1);
  display.setCursor(4, 4);
  display.print("OLED OK");
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 24);
  display.print("SDA GPIO21");
  display.setCursor(0, 38);
  display.print("SCL GPIO22");
  display.display();
  delay(1200);

  ready_ = true;
  Serial.print("[display] SSD1306 OLED ready at 0x");
  Serial.println(oledAddress, HEX);
  Serial.println("[display] OLED shares SDA->GPIO21, SCL->GPIO22 with SHT31");
  return true;
#else
  ready_ = false;
  return false;
#endif
}

void DisplayService::showBoot(const char* deviceCode, const char* zoneCode) {
  if (!ready_) {
    return;
  }
  display.clearDisplay();
  drawHeader("ColdChain IoT");
  display.setTextSize(1);
  display.setCursor(0, 18);
  display.print("Device: ");
  display.print(deviceCode);
  display.setCursor(0, 32);
  display.print("Zone: ");
  display.print(zoneCode);
  display.setCursor(0, 50);
  display.print("Booting...");
  flush();
}

void DisplayService::showSensorError(bool networkConnected) {
  if (!ready_) {
    return;
  }
  display.clearDisplay();
  drawHeader("Sensor Error");
  display.setTextSize(1);
  display.setCursor(0, 22);
  display.print("SHT31 not found");
  display.setCursor(0, 42);
  display.print(networkConnected ? "NET:ON" : "NET:OFF");
  flush();
}

void DisplayService::showReading(float temperature, float humidity,
                                 bool networkConnected, int rssi) {
  if (!ready_) {
    return;
  }

  display.clearDisplay();
  drawHeader("ColdChain Node");

  display.setTextSize(2);
  display.setCursor(0, 18);
  display.print(String(temperature, 1));
  display.print("C");

  display.setTextSize(1);
  display.setCursor(78, 20);
  display.print("Temp");

  display.setTextSize(2);
  display.setCursor(0, 40);
  display.print(String(humidity, 1));
  display.print("%");

  display.setTextSize(1);
  display.setCursor(78, 42);
  display.print("Hum");
  display.setCursor(78, 54);
  display.print(networkLabel(networkConnected, rssi));

  flush();
}

void DisplayService::drawHeader(const char* title) {
  if (!ready_) {
    return;
  }
  display.setTextSize(1);
  display.setCursor(0, 0);
  display.print(title);
  display.drawLine(0, 10, CCG_OLED_WIDTH - 1, 10, SSD1306_WHITE);
}

void DisplayService::flush() {
  if (ready_) {
    display.display();
  }
}
