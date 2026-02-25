package com.coldchain.guardian.contract.enums;

public enum AlertSeverity {
    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高");

    private final int value;
    private final String description;

    AlertSeverity(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}