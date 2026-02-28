package com.coldchain.guardian.contract.dto.monitor;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrendPointDTO {
    private String time;
    private BigDecimal temperature;
    private BigDecimal humidity;
}