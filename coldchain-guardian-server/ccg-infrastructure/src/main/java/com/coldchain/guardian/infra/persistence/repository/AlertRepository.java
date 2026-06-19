package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

    public void updateStatus(Long id, String status, LocalDateTime handleTime, String handleRemark) {
        LambdaUpdateWrapper<AlertEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AlertEntity::getId, id)
                .set(AlertEntity::getStatus, status)
                .set(AlertEntity::getHandleTime, handleTime)
                .set(AlertEntity::getHandleRemark, handleRemark)
                .set(AlertEntity::getUpdateTime, LocalDateTime.now());
        alertMapper.update(null, updateWrapper);
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
        LambdaQueryWrapper<AlertEntity> queryWrapper = buildCommonQueryWrapper(deviceId, alertType, alertLevel, status, startTime, endTime);

        // 使用MyBatis-Plus的分页插件
        Page<AlertEntity> pageInfo = new Page<>(page, size);
        Page<AlertEntity> result = alertMapper.selectPage(pageInfo, queryWrapper);
        return result.getRecords();
    }

    /**
     * 根据设备ID查询告警总数
     */
    public long countByDeviceId(Long deviceId, String alertType, String alertLevel, String status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AlertEntity> queryWrapper = buildCommonQueryWrapper(deviceId, alertType, alertLevel, status, startTime, endTime);
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
            queryWrapper.ge(AlertEntity::getCreateTime, startTime);  // 修改为使用getCreateTime
        }
        if (endTime != null) {
            queryWrapper.le(AlertEntity::getCreateTime, endTime);  // 修改为使用getCreateTime
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
        queryWrapper.orderByDesc(AlertEntity::getCreateTime);  // 修改为使用getCreateTime
        return alertMapper.selectList(queryWrapper);
    }

    /**
     * 查询紧急告警（未处理的紧急和高危告警）
     */
    public List<AlertEntity> findUrgentAlerts() {
        LambdaQueryWrapper<AlertEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertEntity::getStatus, "UNHANDLED")
                   .and(wrapper -> wrapper.eq(AlertEntity::getAlertLevel, "CRITICAL")
                                         .or()
                                         .eq(AlertEntity::getAlertLevel, "HIGH"));
        queryWrapper.orderByDesc(AlertEntity::getCreateTime);  // 修改为使用getCreateTime
        return alertMapper.selectList(queryWrapper);
    }

    /**
     * 构建通用查询条件
     */
    private LambdaQueryWrapper<AlertEntity> buildCommonQueryWrapper(Long deviceId, String alertType, String alertLevel, String status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AlertEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (deviceId != null) {
            queryWrapper.eq(AlertEntity::getDeviceId, deviceId);
        }
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
            queryWrapper.ge(AlertEntity::getCreateTime, startTime);  // 修改为使用getCreateTime
        }
        if (endTime != null) {
            queryWrapper.le(AlertEntity::getCreateTime, endTime);  // 修改为使用getCreateTime
        }

        queryWrapper.orderByDesc(AlertEntity::getCreateTime);  // 修改为使用getCreateTime
        return queryWrapper;
    }
}
