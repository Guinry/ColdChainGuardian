package com.coldchain.guardian.contract.dto.dashboard;

import lombok.Data;

@Data
public class KpiStatDto {
    private String key;
    private String title;
    private String value;
    private String trendText;
    private String trendIcon;
    private String trendClass;
    private String icon;
    private String iconClass;
    private Object[] sparklineData;
}