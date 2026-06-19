#pragma once

#include <Arduino.h>

#include "Telemetry.h"

class ApiClient {
 public:
  ApiClient(const char* baseUrl, const char* telemetryPath, const char* authToken);

  bool postTelemetry(const TelemetryReading& reading, String* responseBody = nullptr);

 private:
  const char* baseUrl_;
  const char* telemetryPath_;
  const char* authToken_;

  String telemetryUrl() const;
  String buildPayload(const TelemetryReading& reading) const;
};
