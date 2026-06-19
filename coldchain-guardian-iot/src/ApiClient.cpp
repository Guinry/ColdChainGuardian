#include "ApiClient.h"

#include <ArduinoJson.h>
#include <HTTPClient.h>
#include <WiFiClient.h>
#include <WiFiClientSecure.h>

ApiClient::ApiClient(const char* baseUrl, const char* telemetryPath, const char* authToken)
    : baseUrl_(baseUrl), telemetryPath_(telemetryPath), authToken_(authToken) {}

bool ApiClient::postTelemetry(const TelemetryReading& reading, String* responseBody) {
  String url = telemetryUrl();
  String payload = buildPayload(reading);

  Serial.print("[api] POST ");
  Serial.println(url);
  Serial.print("[api] payload: ");
  Serial.println(payload);

  HTTPClient http;
  WiFiClient httpClient;
  WiFiClientSecure httpsClient;

  bool started = false;
  if (url.startsWith("https://")) {
    httpsClient.setInsecure();
    started = http.begin(httpsClient, url);
  } else {
    started = http.begin(httpClient, url);
  }

  if (!started) {
    Serial.println("[api] http begin failed");
    return false;
  }

  http.addHeader("Content-Type", "application/json");
  if (String(authToken_).length() > 0) {
    http.addHeader("Authorization", String("Bearer ") + authToken_);
  }

  int statusCode = http.POST(payload);
  String body = http.getString();
  http.end();

  Serial.print("[api] status=");
  Serial.println(statusCode);

  if (responseBody != nullptr) {
    *responseBody = body;
  }

  return statusCode >= 200 && statusCode < 300;
}

String ApiClient::telemetryUrl() const {
  String base = String(baseUrl_);
  String path = String(telemetryPath_);

  if (base.endsWith("/") && path.startsWith("/")) {
    base.remove(base.length() - 1);
  } else if (!base.endsWith("/") && !path.startsWith("/")) {
    base += "/";
  }

  return base + path;
}

String ApiClient::buildPayload(const TelemetryReading& reading) const {
  JsonDocument doc;
  doc["deviceCode"] = reading.deviceCode;
  doc["zoneCode"] = reading.zoneCode;
  doc["temperature"] = roundf(reading.temperature * 100.0f) / 100.0f;
  doc["humidity"] = roundf(reading.humidity * 100.0f) / 100.0f;
  doc["rssi"] = reading.rssi;
  doc["status"] = reading.status;
  doc["uptimeMs"] = reading.uptimeMs;

  if (reading.reportedAt.length() > 0) {
    doc["reportedAt"] = reading.reportedAt;
  }

  String payload;
  serializeJson(doc, payload);
  return payload;
}
