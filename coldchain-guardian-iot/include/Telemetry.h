#pragma once

#include <Arduino.h>

struct TelemetryReading {
  const char* deviceCode;
  const char* zoneCode;
  float temperature;
  float humidity;
  int rssi;
  const char* status;
  unsigned long uptimeMs;
  String reportedAt;
};
