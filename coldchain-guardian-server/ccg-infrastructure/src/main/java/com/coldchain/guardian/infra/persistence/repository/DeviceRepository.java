package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DeviceRepository {

    @Autowired
    private DeviceMapper deviceMapper;

    /**
     * 保存设备
     */
    public void save(DeviceEntity device) {
        if (device.getId() == null) {
            deviceMapper.insert(device);
        } else {
            deviceMapper.updateById(device);
        }
    }

    /**
     * 根据ID查找设备
     */
    public DeviceEntity findById(Long id) {
        return deviceMapper.selectById(id);
    }

    /**
     * 根据设备编码查找设备
     */
    public DeviceEntity findByDeviceCode(String deviceCode) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getDeviceCode, deviceCode);
        return deviceMapper.selectOne(queryWrapper);
    }

    /**
     * 根据设备名称查找设备
     */
    public List<DeviceEntity> findByDeviceName(String deviceName) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getDeviceName, deviceName);
        return deviceMapper.selectList(queryWrapper);
    }

    /**
     * 根据设备类型查找设备
     */
    public List<DeviceEntity> findByDeviceType(String deviceType) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getDeviceType, deviceType);
        return deviceMapper.selectList(queryWrapper);
    }

    /**
     * 根据库区ID查找设备
     */
    public List<DeviceEntity> findByAreaId(Long areaId) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getAreaId, areaId);
        return deviceMapper.selectList(queryWrapper);
    }

    /**
     * 根据启用状态查找设备
     */
    public List<DeviceEntity> findByEnabled(Integer enabled) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getEnabled, enabled);
        return deviceMapper.selectList(queryWrapper);
    }

    /**
     * 根据在线状态查找设备
     */
    public List<DeviceEntity> findByOnlineStatus(Integer onlineStatus) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getOnlineStatus, onlineStatus);
        return deviceMapper.selectList(queryWrapper);
    }

    /**
     * 查找所有设备
     */
    public List<DeviceEntity> findAll() {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(DeviceEntity::getId);
        return deviceMapper.selectList(queryWrapper);
    }

    /**
     * 根据ID删除设备
     */
    public void deleteById(Long id) {
        deviceMapper.deleteById(id);
    }

    /**
     * 检查设备编码是否存在（排除指定ID的设备）
     */
    public boolean existsByDeviceCodeExcludingId(String deviceCode, Long excludeId) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getDeviceCode, deviceCode);
        if (excludeId != null) {
            queryWrapper.ne(DeviceEntity::getId, excludeId);
        }
        return deviceMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查设备名称是否存在（排除指定ID的设备）
     */
    public boolean existsByDeviceNameExcludingId(String deviceName, Long excludeId) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getDeviceName, deviceName);
        if (excludeId != null) {
            queryWrapper.ne(DeviceEntity::getId, excludeId);
        }
        return deviceMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 根据多种条件查询设备（支持分页）
     */
    public List<DeviceEntity> findByConditions(String keyword, String deviceType, Integer onlineStatus,
                                             Integer enabled, Integer alarmEnabled, Long areaId, Integer offset, Integer limit) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（设备编码或设备名称）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(DeviceEntity::getDeviceName, keyword)
                .or()
                .like(DeviceEntity::getDeviceCode, keyword)
            );
        }

        // 设备类型过滤
        if (deviceType != null && !deviceType.trim().isEmpty()) {
            queryWrapper.eq(DeviceEntity::getDeviceType, deviceType);
        }

        // 启用状态过滤
        if (enabled != null) {
            queryWrapper.eq(DeviceEntity::getEnabled, enabled);
        }

        // 告警启用状态过滤
        if (alarmEnabled != null) {
            queryWrapper.eq(DeviceEntity::getAlarmEnabled, alarmEnabled);
        }

        // 在线状态过滤
        if (onlineStatus != null) {
            queryWrapper.eq(DeviceEntity::getOnlineStatus, onlineStatus);
        }

        // 库区ID过滤
        if (areaId != null) {
            queryWrapper.eq(DeviceEntity::getAreaId, areaId);
        }

        // 添加排序
        queryWrapper.orderByDesc(DeviceEntity::getId);

        // 如果提供了偏移量和限制，则进行分页查询
        if (offset != null && limit != null) {
            return deviceMapper.selectList(queryWrapper.last("LIMIT " + limit + " OFFSET " + offset));
        } else {
            return deviceMapper.selectList(queryWrapper);
        }
    }

    /**
     * 根据多种条件统计设备数量
     */
    public int countByConditions(String keyword, String deviceType, Integer onlineStatus,
                               Integer enabled, Integer alarmEnabled, Long areaId) {
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（设备编码或设备名称）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(DeviceEntity::getDeviceName, keyword)
                .or()
                .like(DeviceEntity::getDeviceCode, keyword)
            );
        }

        // 设备类型过滤
        if (deviceType != null && !deviceType.trim().isEmpty()) {
            queryWrapper.eq(DeviceEntity::getDeviceType, deviceType);
        }

        // 启用状态过滤
        if (enabled != null) {
            queryWrapper.eq(DeviceEntity::getEnabled, enabled);
        }

        // 告警启用状态过滤
        if (alarmEnabled != null) {
            queryWrapper.eq(DeviceEntity::getAlarmEnabled, alarmEnabled);
        }

        // 在线状态过滤
        if (onlineStatus != null) {
            queryWrapper.eq(DeviceEntity::getOnlineStatus, onlineStatus);
        }

        // 库区ID过滤
        if (areaId != null) {
            queryWrapper.eq(DeviceEntity::getAreaId, areaId);
        }

        return Math.toIntExact(deviceMapper.selectCount(queryWrapper));
    }
}