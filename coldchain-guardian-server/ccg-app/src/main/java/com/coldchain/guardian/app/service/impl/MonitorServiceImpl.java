package com.coldchain.guardian.app.service;

import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.monitor.MonitorDeviceDTO;
import com.coldchain.guardian.contract.dto.monitor.MonitorSummaryDTO;
import com.coldchain.guardian.contract.dto.monitor.TrendPointDTO;
import com.coldchain.guardian.infra.persistence.mapper.MonitorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private MonitorMapper monitorMapper;

    @Override
    public MonitorSummaryDTO getSummary() {
        return monitorMapper.getMonitorSummary();
    }

    @Override
    public PageResponse<MonitorDeviceDTO> getMonitorDevices(Integer page, Integer size, Long areaId, Boolean online, Boolean alarming, String keyword, String deviceType) {
        // 计算总数
        int total = monitorMapper.countMonitorDevices(areaId, online, alarming, keyword, deviceType);

        // 分页查询
        int offset = (page - 1) * size;
        List<MonitorDeviceDTO> list = monitorMapper.selectMonitorDevices(areaId, online, alarming, keyword, deviceType, size, offset);

        return new PageResponse<>(list, total, page, size);
    }

    @Override
    public List<TrendPointDTO> getDeviceTrend(Long deviceId, LocalDateTime from, LocalDateTime to, Integer interval) {
        // 验证设备是否存在
        // 这里可以加入验证设备存在的逻辑

        // 将LocalDateTime转换为字符串格式传递给mapper
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return monitorMapper.selectDeviceTrend(deviceId, from.format(formatter), to.format(formatter), interval);
    }
}