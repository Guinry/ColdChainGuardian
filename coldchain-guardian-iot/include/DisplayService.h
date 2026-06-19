#pragma once

#include <Arduino.h>

class DisplayService {
public:
  bool begin();
  void showBoot(const char* deviceCode, const char* zoneCode);
  void showSensorError(bool networkConnected);
  void showReading(float temperature, float humidity, bool networkConnected,
                   int rssi);

private:
  bool ready_ = false;
  void drawHeader(const char* title);
  void flush();
};
