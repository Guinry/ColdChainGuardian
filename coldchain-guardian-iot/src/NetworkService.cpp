#include "NetworkService.h"

#include <time.h>

#include "Config.h"

NetworkService::NetworkService(const char* ssid, const char* password)
    : ssid_(ssid), password_(password) {}

void NetworkService::begin() {
  WiFi.mode(WIFI_STA);
  WiFi.setSleep(false);
  connectOnce();
}

void NetworkService::ensureConnected() {
  if (isConnected()) {
    return;
  }

  Serial.println("[network] Wi-Fi disconnected, reconnecting");
  connectOnce();
}

bool NetworkService::connectOnce() {
  if (String(ssid_).length() == 0 || String(ssid_) == "your-wifi-ssid") {
    Serial.println("[network] Wi-Fi SSID is not configured");
    return false;
  }

  WiFi.disconnect(false);
  WiFi.begin(ssid_, password_);

  Serial.print("[network] connecting to ");
  Serial.println(ssid_);

  unsigned long startAt = millis();
  while (WiFi.status() != WL_CONNECTED &&
         millis() - startAt < CCG_WIFI_CONNECT_TIMEOUT_MS) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("[network] connected, ip=");
    Serial.print(ipAddress());
    Serial.print(", mac=");
    Serial.print(macAddress());
    Serial.print(", rssi=");
    Serial.println(rssi());
    return true;
  }

  Serial.println("[network] connect timeout");
  return false;
}

bool NetworkService::isConnected() const {
  return WiFi.status() == WL_CONNECTED;
}

int NetworkService::rssi() const {
  return isConnected() ? WiFi.RSSI() : 0;
}

String NetworkService::ipAddress() const {
  return isConnected() ? WiFi.localIP().toString() : "";
}

String NetworkService::macAddress() const {
  return WiFi.macAddress();
}

void NetworkService::syncTime() {
  if (!isConnected()) {
    return;
  }

  configTime(CCG_GMT_OFFSET_SEC, CCG_DAYLIGHT_OFFSET_SEC, CCG_NTP_SERVER_1,
             CCG_NTP_SERVER_2);

  struct tm timeInfo;
  if (getLocalTime(&timeInfo, 5000)) {
    timeSynced_ = true;
    Serial.print("[time] synced: ");
    Serial.println(currentIsoTime());
  } else {
    Serial.println("[time] sync failed, backend can use receive time");
  }
}

String NetworkService::currentIsoTime() const {
  if (!timeSynced_) {
    return "";
  }

  struct tm timeInfo;
  if (!getLocalTime(&timeInfo, 100)) {
    return "";
  }

  char buffer[25];
  strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S", &timeInfo);
  return String(buffer);
}
