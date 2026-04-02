package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.DashboardService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.dashboard.TrendAnalysisRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "仪表盘管理", description = "提供仪表盘统计数据、趋势分析、实时概览等接口")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取综合仪表盘统计
     */
    @Operation(summary = "获取仪表盘统计", description = "获取综合仪表盘统计数据，包括设备、告警、工单等核心指标")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取仪表盘统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取综合趋势分析（POST 方式）
     */
    @Operation(summary = "获取综合趋势分析", description = "获取多维度的综合趋势分析数据（POST 方式）")
    @PostMapping("/trends")
    public ApiResponse<Map<String, Object>> getComprehensiveTrendAnalysisPost(@RequestBody TrendAnalysisRequest request) {
        try {
            Map<String, Object> analysis = dashboardService.getComprehensiveTrendAnalysis(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getInterval(),
                    request.getDimension()
            );
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取趋势分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取综合趋势分析（GET 方式）
     */
    @Operation(summary = "获取综合趋势分析", description = "获取多维度的综合趋势分析数据（GET 方式）")
    @GetMapping("/trends")
    public ApiResponse<Map<String, Object>> getComprehensiveTrendAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval,
            @RequestParam(required = false) String dimension) {
        try {
            Map<String, Object> analysis = dashboardService.getComprehensiveTrendAnalysis(startDate, endDate, interval, dimension);
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取趋势分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取温湿度趋势数据
     */
    @Operation(summary = "获取温湿度趋势", description = "获取温湿度历史趋势数据")
    @GetMapping("/environment-trend")
    public ApiResponse<Map<String, Object>> getEnvironmentTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        try {
            Map<String, Object> trend = dashboardService.getEnvironmentTrend(startDate, endDate, interval);
            return ApiResponse.success(trend);
        } catch (Exception e) {
            return ApiResponse.error("获取温湿度趋势失败：" + e.getMessage());
        }
    }

    /**
     * 获取告警趋势数据
     */
    @Operation(summary = "获取告警趋势", description = "获取告警数量历史趋势数据")
    @GetMapping("/alert-trend")
    public ApiResponse<Map<String, Object>> getAlertTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        try {
            Map<String, Object> trend = dashboardService.getAlertTrend(startDate, endDate, interval);
            return ApiResponse.success(trend);
        } catch (Exception e) {
            return ApiResponse.error("获取告警趋势失败：" + e.getMessage());
        }
    }

    /**
     * 获取工单趋势数据
     */
    @Operation(summary = "获取工单趋势", description = "获取工单数量历史趋势数据")
    @GetMapping("/workorder-trend")
    public ApiResponse<Map<String, Object>> getWorkOrderTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        try {
            Map<String, Object> trend = dashboardService.getWorkOrderTrend(startDate, endDate, interval);
            return ApiResponse.success(trend);
        } catch (Exception e) {
            return ApiResponse.error("获取工单趋势失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备状态趋势数据
     */
    @Operation(summary = "获取设备状态趋势", description = "获取设备状态历史趋势数据")
    @GetMapping("/device-status-trend")
    public ApiResponse<Map<String, Object>> getDeviceStatusTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String interval) {
        try {
            Map<String, Object> trend = dashboardService.getDeviceStatusTrend(startDate, endDate, interval);
            return ApiResponse.success(trend);
        } catch (Exception e) {
            return ApiResponse.error("获取设备状态趋势失败：" + e.getMessage());
        }
    }

    /**
     * 获取库区实时概览
     */
    @Operation(summary = "获取库区实时概览", description = "获取各库区的实时监测数据概览")
    @GetMapping("/areas")
    public ApiResponse<List<?>> getAreaOverview(
            @RequestParam(defaultValue = "realtime") String timeWindow) {
        try {
            List<?> overview = dashboardService.getAreaOverview(timeWindow);
            return ApiResponse.success(overview);
        } catch (Exception e) {
            return ApiResponse.error("获取库区概览失败：" + e.getMessage());
        }
    }

    /**
     * 获取最近告警
     */
    @Operation(summary = "获取最近告警", description = "获取最近 N 条告警记录")
    @GetMapping("/recent-alerts")
    public ApiResponse<List<?>> getRecentAlerts(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<?> alerts = dashboardService.getRecentAlerts(limit);
            return ApiResponse.success(alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取最近告警失败：" + e.getMessage());
        }
    }

    /**
     * 获取待处理工单
     */
    @Operation(summary = "获取待处理工单", description = "获取待处理的工单列表")
    @GetMapping("/pending-orders")
    public ApiResponse<List<?>> getPendingOrders(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<?> orders = dashboardService.getPendingOrders(limit);
            return ApiResponse.success(orders);
        } catch (Exception e) {
            return ApiResponse.error("获取待处理工单失败：" + e.getMessage());
        }
    }
}
