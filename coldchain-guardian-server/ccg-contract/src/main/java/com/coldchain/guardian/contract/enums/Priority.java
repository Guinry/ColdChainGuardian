package com.coldchain.guardian.contract.enums;

public enum Priority {
    LOW("low", "低"),
    MEDIUM("medium", "中"),
    HIGH("high", "高"),
    URGENT("urgent", "紧急");

    private final String code;
    private final String description;

    Priority(String code, String description) {
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