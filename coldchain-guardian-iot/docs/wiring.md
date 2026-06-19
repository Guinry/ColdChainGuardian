# Wiring

## ESP32 and SHT31

| SHT31 pin | ESP32 pin | Note |
| --- | --- | --- |
| VCC | 3V3 | 3.3V power |
| GND | GND | Common ground |
| SDA | GPIO21 | I2C data |
| SCL | GPIO22 | I2C clock |

## Physical placement

- Keep the SHT31 away from ESP32 voltage regulator and USB chip heat.
- Put the sensor at the actual cold-chain measurement point.
- Use a ventilated shell if it is mounted inside a storage area.
- Avoid water drops directly touching the sensor opening.

## Troubleshooting

- `SHT31 not found`: check VCC/GND/SDA/SCL and try another jumper wire.
- `connect timeout`: check Wi-Fi SSID/password and make sure it is 2.4 GHz Wi-Fi.
- `upload failed`: check backend IP, port, firewall, and endpoint path.
