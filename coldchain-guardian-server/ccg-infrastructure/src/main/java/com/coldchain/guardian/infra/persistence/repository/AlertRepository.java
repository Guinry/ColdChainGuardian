package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.mapper.AlertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AlertRepository {

    @Autowired
    private AlertMapper alertMapper;

    /**
     * 保存告警
     */
    public void save(AlertEntity alert) {
        if (alert.getId() == null) {
            alertMapper.insert(alert);
        } else {
            alertMapper.updateById(alert);
        }
    }

    /**
     * 根据ID查找告警
     */
    public AlertEntity findById(Long id) {
        return alertMapper.selectById(id);
    }

    /**
     * 根据设备ID查询告警列表（分页）
     */
    public List<AlertEntity> findByDeviceId(Long deviceId, Integer page, Integer size, String alertType, String alertLevel, String status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AlertEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertEntity::getDeviceId, deviceId);

        if (alertType != null && !alertType.isEmpty()) {
            queryWrapper.eq(AlertEntity::getAlertType, alertType);
        }
        if (alertLevel != null && !alertLevel.isEmpty()) {
            queryWrapper.eq(AlertEntity::getAlertLevel, alertLevel);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(AlertEntity::getStatus, status);
        }
        if (startTime != null) {
            queryWrapper.ge(AlertEntity::getFirstTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AlertEntity::getFirstTime, endTime);
        }

        queryWrapper.orderByDesc(AlertEntity::getFirstTime);

        // 使用MyBatis-Plus的分页插件
        Page<AlertEntity> pageInfo = new Page<>(page, size);
        Page<AlertEntity> result = alertMapper.selectPage(pageInfo, queryWrapper);
        return result.getRecords();
    }

    /**
     * 根据设备ID查询告警总数
     */
    public long countByDeviceId(Long deviceId, String alertType, String alertLevel, String status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AlertEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertEntity::getDeviceId, deviceId);

        if (alertType != null && !alertType.isEmpty()) {
            queryWrapper.eq(AlertEntity::getAlertType, alertType);
        }
        if (alertLevel != null && !alertLevel.isEmpty()) {
            queryWrapper.eq(AlertEntity::getAlertLevel, alertLevel);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(AlertEntity::getStatus, status);
        }
        if (startTime != null) {
            queryWrapper.ge(AlertEntity::getFirstTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AlertEntity::getFirstTime, endTime);
        }

        return alertMapper.selectCount(queryWrapper);
    }

    /**
     * 根据条件统计告警数量
     */
    public long countByConditions(Long deviceId, String status, String alertLevel, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AlertEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (deviceId != null) {
            queryWrapper.eq(AlertEntity::getDeviceId, deviceId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(AlertEntity::getStatus, status);
        }
        if (alertLevel != null && !alertLevel.isEmpty()) {
            queryWrapper.eq(AlertEntity::getAlertLevel, alertLevel);
        }
        if (startTime != null) {
            queryWrapper.ge(AlertEntity::getFirstTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AlertEntity::getFirstTime, endTime);
        }

        return alertMapper.selectCount(queryWrapper);
    }

    /**
     * 根据所有条件统计告警总数
     */
    public long countAllByConditions(Long deviceId, String status, String alertLevel, LocalDateTime startTime, LocalDateTime endTime) {
        return countByConditions(deviceId, status, alertLevel, startTime, endTime);
    }

    /**
     * 删除告警
     */
    public void deleteById(Long id) {
        alertMapper.deleteById(id);
    }

    /**
     * 查询所有告警
     */
    public List<AlertEntity> findAll() {
        LambdaQueryWrapper<AlertEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(AlertEntity::getFirstTime);
        return alertMapper.selectList(queryWrapper);
    }
}