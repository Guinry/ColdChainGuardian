package com.coldchain.guardian.contract.dto.area;

import java.time.LocalDateTime;
import java.util.List;

public class AreaTreeNodeDto {

    private Long id;
    private Long parentId;  // 上级库区ID
    private String areaCode;  // 库区编码
    private String areaName;  // 库区名称
    private String areaLevel;  // 层级：SITE/WAREHOUSE/FLOOR/AREA/BIN
    private String address;  // 地址
    private String locationDesc;  // 位置描述
    private Double temperatureThresholdMin;  // 温度最小阈值
    private Double temperatureThresholdMax;  // 温度最大阈值
    private Double humidityThresholdMin;  // 湿度最小阈值
    private Double humidityThresholdMax;  // 湿度最大阈值
    private Integer alarmEnabled;  // 是否启用告警
    private Integer status; // 状态：1-启用，0-禁用
    private Integer sortNo; // 排序号
    private String remark;  // 备注
    private Long creatorId;  // 创建人ID
    private Long updaterId;  // 更新人ID
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间

    // 子节点列表
    private List<AreaTreeNodeDto> children;

    // constructors
    public AreaTreeNodeDto() {}

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<AreaTreeNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<AreaTreeNodeDto> children) {
        this.children = children;
    }
}