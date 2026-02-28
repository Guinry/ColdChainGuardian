package com.coldchain.guardian.contract.dto.device;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeviceDto {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String model;
    private String manufacturer;
    private String sn;
    private String firmwareVersion;
    private Long areaId;
    private String areaName;  // 新增：库区名称
    private String locationDesc;
    private String thresholdMode;
    private BigDecimal temperatureThresholdMin;
    private BigDecimal temperatureThresholdMax;
    private BigDecimal humidityThresholdMin;
    private BigDecimal humidityThresholdMax;
    private Boolean alarmEnabled;
    private Boolean enabled;
    private Boolean onlineStatus;
    private LocalDateTime lastSeenTime;

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getLocationDesc() {
        return locationDesc;
    }

    public void setLocationDesc(String locationDesc) {
        this.locationDesc = locationDesc;
    }

    public String getThresholdMode() {
        return thresholdMode;
    }

    public void setThresholdMode(String thresholdMode) {
        this.thresholdMode = thresholdMode;
    }

    public BigDecimal getTemperatureThresholdMin() {
        return temperatureThresholdMin;
    }

    public void setTemperatureThresholdMin(BigDecimal temperatureThresholdMin) {
        this.temperatureThresholdMin = temperatureThresholdMin;
    }

    public BigDecimal getTemperatureThresholdMax() {
        return temperatureThresholdMax;
    }

    public void setTemperatureThresholdMax(BigDecimal temperatureThresholdMax) {
        this.temperatureThresholdMax = temperatureThresholdMax;
    }

    public BigDecimal getHumidityThresholdMin() {
        return humidityThresholdMin;
    }

    public void setHumidityThresholdMin(BigDecimal humidityThresholdMin) {
        this.humidityThresholdMin = humidityThresholdMin;
    }

    public BigDecimal getHumidityThresholdMax() {
        return humidityThresholdMax;
    }

    public void setHumidityThresholdMax(BigDecimal humidityThresholdMax) {
        this.humidityThresholdMax = humidityThresholdMax;
    }

    public Boolean getAlarmEnabled() {
        return alarmEnabled;
    }

    public void setAlarmEnabled(Boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(Boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public LocalDateTime getLastSeenTime() {
        return lastSeenTime;
    }

    public void setLastSeenTime(LocalDateTime lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }
}