package com.coldchain.guardian.contract.dto.dashboard;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TrendAnalysisResponse {
    private String startDate;
    private String endDate;
    private String interval;
    private Map<String, Object> environmentTrend;
    private Map<String, Object> alertTrend;
    private Map<String, Object> workOrderTrend;
    private Map<String, Object> deviceStatusTrend;
    private List<KpiStatDto> kpiStats;
}