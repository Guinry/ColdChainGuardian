#include "SensorService.h"

#include <Wire.h>

#include "Pins.h"

bool SensorService::begin() {
  Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);

  if (sht31_.begin(0x44)) {
    address_ = 0x44;
    ready_ = true;
  } else if (sht31_.begin(0x45)) {
    address_ = 0x45;
    ready_ = true;
  } else {
    ready_ = false;
  }

  if (ready_) {
    sht31_.heater(false);
    Serial.print("[sensor] SHT31 ready at 0x");
    Serial.println(address_, HEX);
  }

  return ready_;
}

bool SensorService::read(float& temperature, float& humidity) {
  if (!ready_) {
    return false;
  }

  temperature = sht31_.readTemperature();
  humidity = sht31_.readHumidity();

  return !isnan(temperature) && !isnan(humidity);
}

bool SensorService::isReady() const {
  return ready_;
}

uint8_t SensorService::address() const {
  return address_;
}
