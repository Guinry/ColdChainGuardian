package com.coldchain.guardian.contract.dto.area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateAreaRequestDto {

    private Long parentId;  // 上级库区ID

    @NotBlank(message = "库区编码不能为空")
    @Size(max = 50, message = "库区编码长度不能超过50个字符")
    private String areaCode;  // 库区编码

    @NotBlank(message = "库区名称不能为空")
    @Size(max = 100, message = "库区名称长度不能超过100个字符")
    private String areaName;  // 库区名称

    @NotBlank(message = "库区层级不能为空")
    @Size(max = 20, message = "库区层级长度不能超过20个字符")
    private String areaLevel = "AREA";  // 层级：SITE/WAREHOUSE/FLOOR/AREA/BIN

    private String address;  // 地址

    private String locationDesc;  // 位置描述

    private Double temperatureThresholdMin = -20.00;  // 温度最小阈值
    private Double temperatureThresholdMax = 8.00;  // 温度最大阈值
    private Double humidityThresholdMin = 30.00;  // 湿度最小阈值
    private Double humidityThresholdMax = 70.00;  // 湿度最大阈值

    private Integer alarmEnabled = 1;  // 是否启用告警，默认启用

    private Integer status = 1; // 状态：1-启用，0-禁用

    private Integer sortNo = 0; // 排序号

    private String remark;  // 备注

    // constructors
    public CreateAreaRequestDto() {}

    // getters and setters
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaLevel() {
        return areaLevel;
    }

    public void setAreaLevel(String areaLevel) {
        this.areaLevel = areaLevel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocationDesc() {
        return locationDesc;
    }

    public void setLocationDesc(String locationDesc) {
        this.locationDesc = locationDesc;
    }

    public Double getTemperatureThresholdMin() {
        return temperatureThresholdMin;
    }

    public void setTemperatureThresholdMin(Double temperatureThresholdMin) {
        this.temperatureThresholdMin = temperatureThresholdMin;
    }

    public Double getTemperatureThresholdMax() {
        return temperatureThresholdMax;
    }

    public void setTemperatureThresholdMax(Double temperatureThresholdMax) {
        this.temperatureThresholdMax = temperatureThresholdMax;
    }

    public Double getHumidityThresholdMin() {
        return humidityThresholdMin;
    }

    public void setHumidityThresholdMin(Double humidityThresholdMin) {
        this.humidityThresholdMin = humidityThresholdMin;
    }

    public Double getHumidityThresholdMax() {
        return humidityThresholdMax;
    }

    public void setHumidityThresholdMax(Double humidityThresholdMax) {
        this.humidityThresholdMax = humidityThresholdMax;
    }

    public Integer getAlarmEnabled() {
        return alarmEnabled;
    }

    public void setAlarmEnabled(Integer alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}