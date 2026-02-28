package com.coldchain.guardian.app.service;

import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.monitor.MonitorDeviceDTO;
import com.coldchain.guardian.contract.dto.monitor.MonitorSummaryDTO;
import com.coldchain.guardian.contract.dto.monitor.TrendPointDTO;

import java.util.List;

public interface MonitorService {

    MonitorSummaryDTO getSummary();

    PageResponse<MonitorDeviceDTO> getMonitorDevices(Integer page, Integer size, Long areaId, Boolean online, Boolean alarming, String keyword);

    List<TrendPointDTO> getDeviceTrend(Long deviceId, String from, String to, Integer interval);
}