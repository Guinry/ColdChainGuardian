#pragma once

#include <Arduino.h>
#include <WiFi.h>

class NetworkService {
 public:
  NetworkService(const char* ssid, const char* password);

  void begin();
  void ensureConnected();
  bool isConnected() const;
  int rssi() const;
  String ipAddress() const;
  String macAddress() const;
  void syncTime();
  String currentIsoTime() const;

 private:
  const char* ssid_;
  const char* password_;
  bool timeSynced_ = false;

  bool connectOnce();
};
