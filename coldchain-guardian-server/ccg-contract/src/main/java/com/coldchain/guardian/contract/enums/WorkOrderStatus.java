package com.coldchain.guardian.contract.enums;

public enum WorkOrderStatus {
    CREATED("created", "已创建"),
    ASSIGNED("assigned", "已分配"),
    IN_PROGRESS("in_progress", "处理中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String description;

    WorkOrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}