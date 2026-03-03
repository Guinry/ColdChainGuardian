package com.coldchain.guardian.contract.enums;

public enum WorkOrderStatus {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    VERIFYING("verifying", "待验收"),
    COMPLETED("completed", "已完成"),
    CLOSED("closed", "已关闭");

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