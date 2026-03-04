package com.coldchain.guardian.contract.dto.dashboard;

import java.util.Map;

/**
 * 仪表盘数据传输对象
 */
public class DashboardDto {

    private Map<String, Object> stats;
    private Map<String, Object> trends;
    private Map<String, Object> alerts;
    private Map<String, Object> workOrders;
    private Map<String, Object> devices;
    private Map<String, Object> environmentData;

    // Constructors
    public DashboardDto() {}

    public DashboardDto(Map<String, Object> stats, Map<String, Object> trends) {
        this.stats = stats;
        this.trends = trends;
    }

    // Getters and Setters
    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public Map<String, Object> getTrends() {
        return trends;
    }

    public void setTrends(Map<String, Object> trends) {
        this.trends = trends;
    }

    public Map<String, Object> getAlerts() {
        return alerts;
    }

    public void setAlerts(Map<String, Object> alerts) {
        this.alerts = alerts;
    }

    public Map<String, Object> getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(Map<String, Object> workOrders) {
        this.workOrders = workOrders;
    }

    public Map<String, Object> getDevices() {
        return devices;
    }

    public void setDevices(Map<String, Object> devices) {
        this.devices = devices;
    }

    public Map<String, Object> getEnvironmentData() {
        return environmentData;
    }

    public void setEnvironmentData(Map<String, Object> environmentData) {
        this.environmentData = environmentData;
    }
}