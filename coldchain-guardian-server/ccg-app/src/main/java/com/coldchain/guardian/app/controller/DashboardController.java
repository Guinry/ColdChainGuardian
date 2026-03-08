package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.DashboardService;
import com.coldchain.guardian.contract.dto.dashboard.AreaOverviewDto;
import com.coldchain.guardian.contract.dto.dashboard.PendingOrderDto;
import com.coldchain.guardian.contract.dto.dashboard.RecentAlertDto;
import com.coldchain.guardian.contract.dto.dashboard.TrendAnalysisRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取综合仪表盘统计
     */
    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    /**
     * 获取综合趋势分析
     */
    @PostMapping("/trends")
    public Map<String, Object> getComprehensiveTrendAnalysis(@RequestBody TrendAnalysisRequest request) {
        return dashboardService.getComprehensiveTrendAnalysis(
                request.getStartDate(),
                request.getEndDate(),
                request.getInterval(),
                request.getDimension()
        );
    }

    /**
     * 获取综合趋势分析（GET方式）
     */
    @GetMapping("/trends")
    public Map<String, Object> getComprehensiveTrendAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval,
            @RequestParam(required = false) String dimension) {
        return dashboardService.getComprehensiveTrendAnalysis(startDate, endDate, interval, dimension);
    }

    /**
     * 获取温湿度趋势数据
     */
    @GetMapping("/environment-trend")
    public Map<String, Object> getEnvironmentTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        return dashboardService.getEnvironmentTrend(startDate, endDate, interval);
    }

    /**
     * 获取告警趋势数据
     */
    @GetMapping("/alert-trend")
    public Map<String, Object> getAlertTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        return dashboardService.getAlertTrend(startDate, endDate, interval);
    }

    /**
     * 获取工单趋势数据
     */
    @GetMapping("/workorder-trend")
    public Map<String, Object> getWorkOrderTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        return dashboardService.getWorkOrderTrend(startDate, endDate, interval);
    }

    /**
     * 获取设备状态趋势数据
     */
    @GetMapping("/device-status-trend")
    public Map<String, Object> getDeviceStatusTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        return dashboardService.getDeviceStatusTrend(startDate, endDate, interval);
    }

    /**
     * 获取库区实时概览 (支持按时间窗口过滤)
     */
    @GetMapping("/areas")
    public Map<String, Object> getAreaOverview(
            @RequestParam(defaultValue = "realtime") String timeWindow) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", dashboardService.getAreaOverview(timeWindow));
        return response;
    }

    /**
     * 获取最近告警 (默认返回前5条)
     */
    @GetMapping("/recent-alerts")
    public Map<String, Object> getRecentAlerts(
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", dashboardService.getRecentAlerts(limit));
        return response;
    }

    /**
     * 获取待处理工单 (默认返回前5条)
     */
    @GetMapping("/pending-orders")
    public Map<String, Object> getPendingOrders(
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", dashboardService.getPendingOrders(limit));
        return response;
    }
}
