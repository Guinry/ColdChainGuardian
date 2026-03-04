package com.coldchain.guardian.contract.dto.dashboard;

import lombok.Data;

@Data
public class TrendAnalysisRequest {
    private String startDate;
    private String endDate;
    private String interval = "daily"; // hourly, daily, weekly, monthly
    private String dimension; // 分析维度
    private String[] dimensions; // 多维度数组
}