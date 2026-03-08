package com.coldchain.guardian.contract.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待处理工单数据传输对象
 */
@Data
public class PendingOrderDto {
    private String orderId;
    private String alert;
    private String assignee;
    private String status; // 待处理、处理中
    private String updatedAt;

    public PendingOrderDto(String orderId, String alert, String assignee, String status, String updatedAt) {
        this.orderId = orderId;
        this.alert = alert;
        this.assignee = assignee;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}