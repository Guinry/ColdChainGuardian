# Telemetry protocol

Default endpoint:

```http
POST /api/iot/telemetry
Content-Type: application/json
```

Payload:

```json
{
  "deviceCode": "TH-A01-001",
  "zoneCode": "A01",
  "temperature": -18.60,
  "humidity": 68.20,
  "rssi": -56,
  "status": "ONLINE",
  "uptimeMs": 120000,
  "reportedAt": "2026-05-18T14:30:00"
}
```

Field description:

| Field | Type | Description |
| --- | --- | --- |
| deviceCode | string | Device code in ColdChain Guardian |
| zoneCode | string | Warehouse area code |
| temperature | number | Temperature in Celsius |
| humidity | number | Relative humidity, %RH |
| rssi | number | Wi-Fi signal strength |
| status | string | Device status |
| uptimeMs | number | ESP32 uptime in milliseconds |
| reportedAt | string | Local ISO time after NTP sync |
