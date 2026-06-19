#include <Arduino.h>

#include "ApiClient.h"
#include "Config.h"
#include "DisplayService.h"
#include "NetworkService.h"
#include "SensorService.h"

SensorService sensorService;
DisplayService displayService;
NetworkService networkService(CCG_WIFI_SSID, CCG_WIFI_PASSWORD);
ApiClient apiClient(CCG_API_BASE_URL, CCG_API_TELEMETRY_PATH, CCG_API_AUTH_TOKEN);

unsigned long lastSampleAt = 0;
unsigned long lastNetworkCheckAt = 0;

static void printBootInfo() {
  Serial.println();
  Serial.println("ColdChain Guardian IoT node");
  Serial.print("Device code: ");
  Serial.println(CCG_DEVICE_CODE);
  Serial.print("Zone code: ");
  Serial.println(CCG_ZONE_CODE);
  Serial.print("Backend: ");
  Serial.print(CCG_API_BASE_URL);
  Serial.println(CCG_API_TELEMETRY_PATH);
  Serial.println("SHT31 wiring: VCC->3V3, GND->GND, SDA->GPIO21, SCL->GPIO22");
  Serial.println();
}

static void reportOnce() {
  TelemetryReading reading;
  reading.deviceCode = CCG_DEVICE_CODE;
  reading.zoneCode = CCG_ZONE_CODE;
  reading.status = "ONLINE";
  reading.uptimeMs = millis();
  reading.rssi = networkService.rssi();
  reading.reportedAt = networkService.currentIsoTime();

  if (!sensorService.read(reading.temperature, reading.humidity)) {
    Serial.println("[sensor] read failed");
    displayService.showSensorError(networkService.isConnected());
    return;
  }

  Serial.print("[sensor] temperature=");
  Serial.print(reading.temperature, 2);
  Serial.print(" C, humidity=");
  Serial.print(reading.humidity, 2);
  Serial.print(" %RH, rssi=");
  Serial.println(reading.rssi);

  displayService.showReading(reading.temperature, reading.humidity,
                             networkService.isConnected(), reading.rssi);

  if (!networkService.isConnected()) {
    Serial.println("[network] offline, skip upload");
    return;
  }

  String response;
  bool ok = apiClient.postTelemetry(reading, &response);
  Serial.print("[api] upload ");
  Serial.println(ok ? "success" : "failed");
  if (response.length() > 0) {
    Serial.print("[api] response: ");
    Serial.println(response);
  }
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  printBootInfo();
  displayService.begin();
  displayService.showBoot(CCG_DEVICE_CODE, CCG_ZONE_CODE);

  if (!sensorService.begin()) {
    Serial.println("[sensor] SHT31 not found. Check wiring or try I2C address 0x45.");
    displayService.showSensorError(networkService.isConnected());
  }

  networkService.begin();
  if (networkService.isConnected()) {
    networkService.syncTime();
  }

  lastSampleAt = millis() - CCG_SAMPLE_INTERVAL_MS;
}

void loop() {
  unsigned long now = millis();

  if (now - lastNetworkCheckAt >= CCG_NETWORK_CHECK_INTERVAL_MS) {
    lastNetworkCheckAt = now;
    networkService.ensureConnected();
  }

  if (now - lastSampleAt >= CCG_SAMPLE_INTERVAL_MS) {
    lastSampleAt = now;
    reportOnce();
  }

  delay(50);
}
