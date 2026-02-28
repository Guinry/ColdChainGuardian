package com.coldchain.guardian.infra.persistence.mapper;

import com.coldchain.guardian.contract.dto.monitor.MonitorDeviceDTO;
import com.coldchain.guardian.contract.dto.monitor.MonitorSummaryDTO;
import com.coldchain.guardian.contract.dto.monitor.TrendPointDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MonitorMapper {

    MonitorSummaryDTO getMonitorSummary();

    int countMonitorDevices(@Param("areaId") Long areaId,
                           @Param("online") Boolean online,
                           @Param("alarming") Boolean alarming,
                           @Param("keyword") String keyword);

    List<MonitorDeviceDTO> selectMonitorDevices(@Param("areaId") Long areaId,
                                              @Param("online") Boolean online,
                                              @Param("alarming") Boolean alarming,
                                              @Param("keyword") String keyword,
                                              @Param("limit") Integer limit,
                                              @Param("offset") Integer offset);

    List<TrendPointDTO> selectDeviceTrend(@Param("deviceId") Long deviceId,
                                         @Param("from") String from,
                                         @Param("to") String to,
                                         @Param("interval") Integer interval);
}