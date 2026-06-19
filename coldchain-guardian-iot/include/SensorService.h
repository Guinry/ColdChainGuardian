#pragma once

#include <Adafruit_SHT31.h>
#include <Arduino.h>

class SensorService {
 public:
  bool begin();
  bool read(float& temperature, float& humidity);
  bool isReady() const;
  uint8_t address() const;

 private:
  Adafruit_SHT31 sht31_;
  bool ready_ = false;
  uint8_t address_ = 0x44;
};
