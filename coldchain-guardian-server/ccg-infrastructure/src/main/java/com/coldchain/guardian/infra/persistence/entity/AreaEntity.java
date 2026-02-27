package com.coldchain.guardian.infra.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@TableName("warehouse_areas")  // 根据DATABASE_SCHEMA.md，表名为warehouse_areas
public class AreaEntity extends BaseEntity {

    @TableField("parent_id")
    private Long parentId;  // 上级库区ID，NULL表示顶级

    @NotBlank(message = "库区编码不能为空")
    @Size(min = 1, max = 50, message = "库区编码长度必须在1-50个字符之间")
    @TableField("area_code")
    private String areaCode;  // 库区编码

    @NotBlank(message = "库区名称不能为空")
    @Size(min = 1, max = 100, message = "库区名称长度必须在1-100个字符之间")
    @TableField("area_name")
    private String areaName;  // 库区名称

    @NotBlank(message = "库区层级不能为空")
    @Size(min = 1, max = 20, message = "库区层级长度必须在1-20个字符之间")
    @TableField("area_level")
    private String areaLevel;  // 层级：SITE/WAREHOUSE/FLOOR/AREA/BIN，默认'AREA'

    @TableField("address")
    private String address;  // 地址（顶级/仓库级可用）

    @TableField("location_desc")
    private String locationDesc;  // 位置描述（如A栋2层东区）

    @TableField("temperature_threshold_min")
    private Double temperatureThresholdMin;  // 温度最小阈值

    @TableField("temperature_threshold_max")
    private Double temperatureThresholdMax;  // 温度最大阈值

    @TableField("humidity_threshold_min")
    private Double humidityThresholdMin;  // 湿度最小阈值

    @TableField("humidity_threshold_max")
    private Double humidityThresholdMax;  // 湿度最大阈值

    @TableField("alarm_enabled")
    private Integer alarmEnabled;  // 是否启用告警，默认1

    @TableField("status")
    private Integer status; // 1-启用，0-禁用

    @TableField("sort_no")
    private Integer sortNo; // 排序号

    @TableField("remark")
    private String remark;  // 备注

    @TableField("creator_id")
    private Long creatorId;  // 创建人ID

    @TableField("updater_id")
    private Long updaterId;  // 更新人ID

    // constructors
    public AreaEntity() {}

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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getUpdaterId() {
        return updaterId;
    }

    public void setUpdaterId(Long updaterId) {
        this.updaterId = updaterId;
    }
}