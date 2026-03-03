package com.coldchain.guardian.app.service;

import com.coldchain.guardian.infra.persistence.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.coldchain.guardian.infra.persistence.entity.AlertEntity;

/**
 * 告警分析服务
 * 提供告警趋势分析、模式识别等功能
 */
@Service
public class AlertAnalysisService {

    private final AlertRepository alertRepository;

    @Autowired
    public AlertAnalysisService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * 获取告警趋势分析
     */
    public Map<String, Object> getAlertTrendAnalysis(String period) {
        List<AlertEntity> alerts = alertRepository.findAll();

        // 根据时间段分析告警趋势
        Map<String, Long> trendData = alerts.stream()
            .collect(Collectors.groupingBy(
                alert -> getPeriodKey(alert.getFirstTime(), period),
                Collectors.counting()
            ));

        // 统计告警类型分布
        Map<String, Long> typeDistribution = alerts.stream()
            .collect(Collectors.groupingBy(
                AlertEntity::getAlertType,
                Collectors.counting()
            ));

        // 统计设备告警排行
        Map<String, Long> deviceRanking = alerts.stream()
            .collect(Collectors.groupingBy(
                AlertEntity::getDeviceName,
                Collectors.counting()
            ));

        // 按级别统计
        Map<String, Long> levelStats = alerts.stream()
            .collect(Collectors.groupingBy(
                AlertEntity::getAlertLevel,
                Collectors.counting()
            ));

        // 计算告警增长率
        double growthRate = calculateGrowthRate(alerts);

        // 构建分析结果
        Map<String, Object> result = new HashMap<>();
        result.put("trendData", trendData);
        result.put("typeDistribution", typeDistribution);
        result.put("deviceRanking", deviceRanking);
        result.put("levelStats", levelStats);
        result.put("totalAlerts", (long) alerts.size());
        result.put("growthRate", growthRate);
        return result;
    }

    /**
     * 获取告警重复性分析
     * 识别频繁出现的告警模式
     */
    public Map<String, Object> getRecurringAlertAnalysis() {
        List<AlertEntity> alerts = alertRepository.findAll();

        // 按设备和告警类型分组，找出高频告警
        Map<String, List<AlertEntity>> groupedAlerts = alerts.stream()
            .collect(Collectors.groupingBy(
                alert -> alert.getDeviceName() + "|" + alert.getAlertType()
            ));

        // 找出重复次数超过阈值的告警
        Map<String, Long> recurringAlerts = groupedAlerts.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 3) // 超过3次的认为是重复告警
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (long) entry.getValue().size()
            ));

        // 计算重复告警占比
        long totalAlerts = alerts.size();
        long recurringCount = recurringAlerts.values().stream().mapToLong(Long::longValue).sum();
        double recurringPercentage = totalAlerts > 0 ? (double) recurringCount / totalAlerts * 100 : 0;

        // 构建分析结果
        Map<String, Object> result = new HashMap<>();
        result.put("recurringAlerts", recurringAlerts);
        result.put("recurringPercentage", recurringPercentage);
        result.put("totalCount", (long) alerts.size());
        return result;
    }

    /**
     * 获取设备健康度评分
     * 基于告警频率和级别评估设备健康状况
     */
    public Map<String, Object> getDeviceHealthScore() {
        List<AlertEntity> alerts = alertRepository.findAll();

        // 按设备分组统计
        Map<String, List<AlertEntity>> alertsByDevice = alerts.stream()
            .collect(Collectors.groupingBy(AlertEntity::getDeviceName));

        // 计算每个设备的健康得分
        Map<String, Double> healthScores = alertsByDevice.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> calculateHealthScore(entry.getValue())
            ));

        // 按健康状况分类
        Map<String, List<String>> devicesByHealthStatus = healthScores.entrySet().stream()
            .collect(Collectors.groupingBy(
                entry -> getHealthStatusLabel(entry.getValue()),
                Collectors.mapping(Map.Entry::getKey, Collectors.toList())
            ));

        // 构建分析结果
        Map<String, Object> result = new HashMap<>();
        result.put("healthScores", healthScores);
        result.put("devicesByHealthStatus", devicesByHealthStatus);
        result.put("deviceCount", (long) alertsByDevice.size());
        return result;
    }

    /**
     * 获取告警根因分析
     * 识别可能导致告警的根本原因
     */
    public Map<String, Object> getRootCauseAnalysis() {
        List<AlertEntity> alerts = alertRepository.findAll();

        // 按时间窗口分组分析告警集群
        // 将LocalDateTime转换为String格式以确保类型一致
        Map<String, List<AlertEntity>> alertClusters = alerts.stream()
            .collect(Collectors.groupingBy(
                alert -> alert.getFirstTime().withMinute(0).withSecond(0).toString() // 按小时聚合，转换为字符串
            ));

        // 找出同时发生的相关告警
        Map<String, List<String>> correlatedAlerts = alertClusters.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1) // 多个告警同时发生
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(AlertEntity::getAlertType)
                    .distinct()
                    .collect(Collectors.toList())
            ));

        // 构建分析结果
        Map<String, Object> result = new HashMap<>();
        result.put("correlatedAlerts", correlatedAlerts);
        result.put("clusterCount", (long) alertClusters.size());
        result.put("totalAlerts", (long) alerts.size());
        return result;
    }

    /**
     * 计算告警增长率
     */
    private double calculateGrowthRate(List<AlertEntity> alerts) {
        if (alerts.size() < 2) {
            return 0.0;
        }

        // 获取最早的告警时间
        LocalDateTime earliest = alerts.stream()
            .map(AlertEntity::getFirstTime)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());

        // 获取最晚的告警时间
        LocalDateTime latest = alerts.stream()
            .map(AlertEntity::getFirstTime)
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());

        // 计算时间跨度内的告警数量变化率
        long daysBetween = java.time.Duration.between(earliest, latest).toDays();
        if (daysBetween <= 0) {
            return 0.0;
        }

        double alertsPerDay = (double) alerts.size() / daysBetween;
        return Math.round(alertsPerDay * 100.0) / 100.0;
    }

    /**
     * 获取周期键值（用于分组统计）
     */
    private String getPeriodKey(LocalDateTime time, String period) {
        return switch (period.toLowerCase()) {
            case "hourly" -> time.getYear() + "-" + time.getMonthValue() + "-" + time.getDayOfMonth() + " " + time.getHour();
            case "daily" -> time.getYear() + "-" + time.getMonthValue() + "-" + time.getDayOfMonth();
            case "weekly" -> {
                int week = (time.getDayOfYear() - 1) / 7 + 1;
                yield time.getYear() + "-W" + week;
            }
            case "monthly" -> time.getYear() + "-" + time.getMonthValue();
            default -> time.getYear() + "-" + time.getMonthValue();
        };
    }

    /**
     * 计算设备健康得分
     * 分数越低表示设备越不健康
     */
    private double calculateHealthScore(List<AlertEntity> deviceAlerts) {
        if (deviceAlerts.isEmpty()) {
            return 100.0; // 无告警表示非常健康
        }

        double score = 100.0; // 基础分数

        // 按告警级别扣分
        for (AlertEntity alert : deviceAlerts) {
            score -= switch (alert.getAlertLevel()) {
                case "CRITICAL" -> 10.0;
                case "HIGH" -> 5.0;
                case "MEDIUM" -> 2.0;
                case "LOW" -> 1.0;
                default -> 0.0;
            };

            // 如果告警在近期产生，额外扣分
            if (alert.getFirstTime().isAfter(LocalDateTime.now().minusDays(7))) {
                score -= switch (alert.getAlertLevel()) {
                    case "CRITICAL" -> 5.0;
                    case "HIGH" -> 3.0;
                    case "MEDIUM" -> 1.5;
                    case "LOW" -> 0.5;
                    default -> 0.0;
                };
            }
        }

        // 确保分数在合理范围内
        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * 获取健康状态标签
     */
    private String getHealthStatusLabel(double score) {
        if (score >= 80) return "healthy";
        if (score >= 60) return "normal";
        if (score >= 40) return "warning";
        return "critical";
    }
}