package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.enums.WorkOrderStatus;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.TelemetryEntity;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderEntity;
import com.coldchain.guardian.infra.persistence.repository.AlertRepository;
import com.coldchain.guardian.infra.persistence.repository.DeviceRepository;
import com.coldchain.guardian.infra.persistence.repository.TelemetryRepository;
import com.coldchain.guardian.infra.persistence.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class DashboardService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private TelemetryRepository telemetryRepository;

    /**
     * 安全解析日期，处理ISO 8601格式的时间戳
     */
    private LocalDate parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        // 如果包含 'T'，说明是前端传来的带时间的格式，我们只截取前 10 位 (yyyy-MM-dd)
        if (dateStr.contains("T")) {
            dateStr = dateStr.substring(0, 10);
        }
        return LocalDate.parse(dateStr);
    }

    /**
     * 获取综合趋势分析数据
     */
    public Map<String, Object> getComprehensiveTrendAnalysis(String startDate, String endDate, String interval, String dimension) {
        Map<String, Object> result = new HashMap<>();

        // 按指定间隔获取趋势数据
        result.put("environmentTrend", getEnvironmentTrend(startDate, endDate, interval));
        result.put("alertTrend", getAlertTrend(startDate, endDate, interval));
        result.put("workOrderTrend", getWorkOrderTrend(startDate, endDate, interval));
        result.put("deviceStatusTrend", getDeviceStatusTrend(startDate, endDate, interval));

        // 根据分析维度进一步处理数据
        if (dimension != null && !dimension.isEmpty()) {
            // 可以根据具体维度进行额外的数据处理
            result.put("dimension", dimension);
        }

        return result;
    }

    /**
     * 获取温湿度趋势数据
     */
    public Map<String, Object> getEnvironmentTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        // 获取遥测数据
        List<TelemetryEntity> telemetryData = telemetryRepository.findAll();
        List<TelemetryEntity> filteredData = telemetryData.stream()
                .filter(data -> {
                    if (data.getDataTime() == null) return false;
                    LocalDate dataDate = data.getDataTime().toLocalDate();
                    return !dataDate.isBefore(start) && !dataDate.isAfter(end);
                })
                .collect(Collectors.toList());

        // 按时间间隔分组统计
        Map<String, List<TelemetryEntity>> groupedData = filteredData.stream()
                .collect(Collectors.groupingBy(
                        data -> {
                            LocalDate dataDate = data.getDataTime().toLocalDate();
                            return switch (interval.toLowerCase()) {
                                case "daily" -> dataDate.toString();
                                case "weekly" -> getWeekKey(dataDate);
                                case "monthly" -> getMonthKey(dataDate);
                                case "hourly" -> data.getDataTime().truncatedTo(java.time.temporal.ChronoUnit.HOURS).toString();
                                default -> dataDate.toString();
                            };
                        }
                ));

        // 计算每个时间段的平均温湿度
        List<Map<String, Object>> dataPoints = groupedData.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey();
                    List<TelemetryEntity> periodData = entry.getValue();

                    // 计算平均值
                    double avgTemp = periodData.stream()
                            .mapToDouble(data -> data.getTemperature() != null ? data.getTemperature() : 0.0)
                            .average()
                            .orElse(0.0);

                    double avgHumidity = periodData.stream()
                            .mapToDouble(data -> data.getHumidity() != null ? data.getHumidity() : 0.0)
                            .average()
                            .orElse(0.0);

                    // 计算极值
                    Double maxTemp = periodData.stream()
                            .mapToDouble(data -> data.getTemperature() != null ? data.getTemperature() : 0.0)
                            .max()
                            .orElse(0.0);

                    Double minTemp = periodData.stream()
                            .mapToDouble(data -> data.getTemperature() != null ? data.getTemperature() : 0.0)
                            .min()
                            .orElse(0.0);

                    Double maxHumidity = periodData.stream()
                            .mapToDouble(data -> data.getHumidity() != null ? data.getHumidity() : 0.0)
                            .max()
                            .orElse(0.0);

                    Double minHumidity = periodData.stream()
                            .mapToDouble(data -> data.getHumidity() != null ? data.getHumidity() : 0.0)
                            .min()
                            .orElse(0.0);

                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("date", period);
                    dataPoint.put("temperature", Math.round(avgTemp * 100.0) / 100.0);
                    dataPoint.put("humidity", Math.round(avgHumidity * 100.0) / 100.0);
                    dataPoint.put("maxTemperature", Math.round(maxTemp * 100.0) / 100.0);
                    dataPoint.put("minTemperature", Math.round(minTemp * 100.0) / 100.0);
                    dataPoint.put("maxHumidity", Math.round(maxHumidity * 100.0) / 100.0);
                    dataPoint.put("minHumidity", Math.round(minHumidity * 100.0) / 100.0);
                    dataPoint.put("count", (long) periodData.size());

                    return dataPoint;
                })
                .sorted(Comparator.comparing((Map<String, Object> m) -> m.get("date").toString())) // Sort by date
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("data", dataPoints);
        result.put("interval", interval);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());

        return result;
    }

    /**
     * 获取告警趋势数据
     */
    public Map<String, Object> getAlertTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        List<AlertEntity> alerts = alertRepository.findAll();

        // 按时间范围过滤
        List<AlertEntity> filteredAlerts = alerts.stream()
                .filter(alert -> {
                    LocalDate alertDate = alert.getFirstTime().toLocalDate();
                    return !alertDate.isBefore(start) && !alertDate.isAfter(end);
                })
                .collect(Collectors.toList());

        // 按时间间隔分组统计
        Map<String, Map<String, Long>> alertStats = groupAlertsByInterval(filteredAlerts, start, end, interval);

        Map<String, Object> result = new HashMap<>();
        result.put("data", alertStats);
        result.put("total", (long) filteredAlerts.size());
        result.put("interval", interval);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());

        return result;
    }

    /**
     * 获取工单趋势数据
     */
    public Map<String, Object> getWorkOrderTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        List<WorkOrderEntity> workOrders = workOrderRepository.findAll();

        // 按时间范围过滤
        List<WorkOrderEntity> filteredWorkOrders = workOrders.stream()
                .filter(wo -> {
                    if (wo.getCreateTime() == null) return false;
                    LocalDate woDate = wo.getCreateTime().toLocalDate();
                    return !woDate.isBefore(start) && !woDate.isAfter(end);
                })
                .collect(Collectors.toList());

        // 按时间间隔分组统计
        Map<String, Map<String, Long>> workOrderStats = groupWorkOrdersByInterval(filteredWorkOrders, start, end, interval);

        Map<String, Object> result = new HashMap<>();
        result.put("data", workOrderStats);
        result.put("total", (long) filteredWorkOrders.size());
        result.put("interval", interval);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());

        return result;
    }

    /**
     * 获取设备状态趋势数据
     */
    public Map<String, Object> getDeviceStatusTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        List<DeviceEntity> devices = deviceRepository.findAll();

        // 按状态统计设备
        Map<String, Long> statusCount = devices.stream()
                .collect(Collectors.groupingBy(
                        device -> device.getOnlineStatus() != null && device.getOnlineStatus() == 1 ? "online" : "offline",
                        Collectors.counting()
                ));

        // 获取历史设备状态趋势
        List<Map<String, Object>> historicalData = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        for (int i = 0; i <= daysBetween; i++) {
            LocalDate date = start.plusDays(i);

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", date.toString());
            dataPoint.put("online", statusCount.getOrDefault("online", 0L));
            dataPoint.put("offline", statusCount.getOrDefault("offline", 0L));

            historicalData.add(dataPoint);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", historicalData);
        result.put("currentStatus", statusCount);
        result.put("interval", interval);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());

        return result;
    }

    /**
     * 获取综合仪表盘统计
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 告警统计
        List<AlertEntity> alerts = alertRepository.findAll();
        long totalAlerts = alerts.size();
        long unhandledAlerts = alerts.stream()
                .filter(alert -> "UNHANDLED".equals(alert.getStatus()))
                .count();
        long criticalAlerts = alerts.stream()
                .filter(alert -> "CRITICAL".equals(alert.getAlertLevel()))
                .count();
        long todayAlerts = alerts.stream()
                .filter(alert -> alert.getFirstTime().toLocalDate().equals(LocalDate.now()))
                .count();

        // 工单统计
        List<WorkOrderEntity> workOrders = workOrderRepository.findAll();
        long totalWorkOrders = workOrders.size();
        long pendingWorkOrders = workOrders.stream()
                .filter(wo -> WorkOrderStatus.PENDING.getCode().equals(wo.getStatus()))
                .count();
        long processingWorkOrders = workOrders.stream()
                .filter(wo -> WorkOrderStatus.PROCESSING.getCode().equals(wo.getStatus()))
                .count();

        // 设备统计
        List<DeviceEntity> devices = deviceRepository.findAll();
        long totalDevices = devices.size();
        long onlineDevices = devices.stream()
                .filter(device -> device.getOnlineStatus() != null && device.getOnlineStatus() == 1)
                .count(); // 假设1表示在线
        long offlineDevices = totalDevices - onlineDevices;

        // 计算温湿度平均值（用于KPI展示）
        List<TelemetryEntity> telemetryData = telemetryRepository.findAll();
        OptionalDouble avgTemperature = telemetryData.stream()
                .mapToDouble(data -> data.getTemperature() != null ? data.getTemperature() : 0.0)
                .average();
        OptionalDouble avgHumidity = telemetryData.stream()
                .mapToDouble(data -> data.getHumidity() != null ? data.getHumidity() : 0.0)
                .average();

        // 计算统计结果
        stats.put("alerts", Map.of(
                "total", totalAlerts,
                "unhandled", unhandledAlerts,
                "critical", criticalAlerts,
                "today", todayAlerts
        ));

        stats.put("workOrders", Map.of(
                "total", totalWorkOrders,
                "pending", pendingWorkOrders,
                "processing", processingWorkOrders
        ));

        stats.put("devices", Map.of(
                "total", totalDevices,
                "online", onlineDevices,
                "offline", offlineDevices,
                "onlineRate", totalDevices > 0 ? Math.round((double) onlineDevices / totalDevices * 10000) / 100.0 : 0.0
        ));

        stats.put("environment", Map.of(
                "avgTemperature", avgTemperature.isPresent() ? Math.round(avgTemperature.getAsDouble() * 100.0) / 100.0 : 0.0,
                "avgHumidity", avgHumidity.isPresent() ? Math.round(avgHumidity.getAsDouble() * 100.0) / 100.0 : 0.0
        ));

        return stats;
    }

    /**
     * 按时间间隔对告警进行分组统计
     */
    private Map<String, Map<String, Long>> groupAlertsByInterval(List<AlertEntity> alerts, LocalDate start, LocalDate end, String interval) {
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();

        // 为每个时间段创建数据结构
        long periods = switch (interval.toLowerCase()) {
            case "daily" -> ChronoUnit.DAYS.between(start, end) + 1;
            case "weekly" -> (ChronoUnit.WEEKS.between(start, end) + 1);
            case "monthly" -> (ChronoUnit.MONTHS.between(start, end) + 1);
            case "hourly" -> ChronoUnit.HOURS.between(start.atStartOfDay(), end.atStartOfDay()) + 1;
            default -> ChronoUnit.DAYS.between(start, end) + 1;
        };

        // 为每个时间段创建数据结构
        for (long i = 0; i < periods; i++) {
            LocalDate currentDate = start.plusDays(i);
            String periodKey = switch (interval.toLowerCase()) {
                case "daily" -> currentDate.toString();
                case "weekly" -> getWeekKey(currentDate);
                case "monthly" -> getMonthKey(currentDate);
                case "hourly" -> currentDate.atStartOfDay().toString();
                default -> currentDate.toString();
            };

            result.putIfAbsent(periodKey, new HashMap<>());
            result.get(periodKey).put("total", 0L);
            result.get(periodKey).put("critical", 0L);
            result.get(periodKey).put("high", 0L);
            result.get(periodKey).put("medium", 0L);
            result.get(periodKey).put("low", 0L);
        }

        // 统计实际告警数据
        for (AlertEntity alert : alerts) {
            LocalDate alertDate = alert.getFirstTime().toLocalDate();
            if (alertDate.isBefore(start) || alertDate.isAfter(end)) {
                continue;
            }

            String periodKey = switch (interval.toLowerCase()) {
                case "daily" -> alertDate.toString();
                case "weekly" -> getWeekKey(alertDate);
                case "monthly" -> getMonthKey(alertDate);
                case "hourly" -> alert.getFirstTime().truncatedTo(java.time.temporal.ChronoUnit.HOURS).toString();
                default -> alertDate.toString();
            };

            if (result.containsKey(periodKey)) {
                Map<String, Long> periodData = result.get(periodKey);
                periodData.put("total", periodData.get("total") + 1);

                String level = alert.getAlertLevel();
                if ("CRITICAL".equalsIgnoreCase(level)) {
                    periodData.put("critical", periodData.get("critical") + 1);
                } else if ("HIGH".equalsIgnoreCase(level)) {
                    periodData.put("high", periodData.get("high") + 1);
                } else if ("MEDIUM".equalsIgnoreCase(level)) {
                    periodData.put("medium", periodData.get("medium") + 1);
                } else if ("LOW".equalsIgnoreCase(level)) {
                    periodData.put("low", periodData.get("low") + 1);
                }
            }
        }

        return result;
    }

    /**
     * 按时间间隔对工单进行分组统计
     */
    private Map<String, Map<String, Long>> groupWorkOrdersByInterval(List<WorkOrderEntity> workOrders, LocalDate start, LocalDate end, String interval) {
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();

        // 为每个时间段创建数据结构
        long periods = switch (interval.toLowerCase()) {
            case "daily" -> ChronoUnit.DAYS.between(start, end) + 1;
            case "weekly" -> (ChronoUnit.WEEKS.between(start, end) + 1);
            case "monthly" -> (ChronoUnit.MONTHS.between(start, end) + 1);
            case "hourly" -> ChronoUnit.HOURS.between(start.atStartOfDay(), end.atStartOfDay()) + 1;
            default -> ChronoUnit.DAYS.between(start, end) + 1;
        };

        // 为每个时间段创建数据结构
        for (long i = 0; i < periods; i++) {
            LocalDate currentDate = start.plusDays(i);
            String periodKey = switch (interval.toLowerCase()) {
                case "daily" -> currentDate.toString();
                case "weekly" -> getWeekKey(currentDate);
                case "monthly" -> getMonthKey(currentDate);
                case "hourly" -> currentDate.atStartOfDay().toString();
                default -> currentDate.toString();
            };

            result.putIfAbsent(periodKey, new HashMap<>());
            result.get(periodKey).put("total", 0L);
            result.get(periodKey).put("completed", 0L);
            result.get(periodKey).put("pending", 0L);
            result.get(periodKey).put("processing", 0L);
            result.get(periodKey).put("closed", 0L);
        }

        // 统计实际工单数据
        for (WorkOrderEntity workOrder : workOrders) {
            if (workOrder.getCreateTime() == null) continue;

            LocalDate woDate = workOrder.getCreateTime().toLocalDate();
            if (woDate.isBefore(start) || woDate.isAfter(end)) {
                continue;
            }

            String periodKey = switch (interval.toLowerCase()) {
                case "daily" -> woDate.toString();
                case "weekly" -> getWeekKey(woDate);
                case "monthly" -> getMonthKey(woDate);
                case "hourly" -> workOrder.getCreateTime().truncatedTo(java.time.temporal.ChronoUnit.HOURS).toString();
                default -> woDate.toString();
            };

            if (result.containsKey(periodKey)) {
                Map<String, Long> periodData = result.get(periodKey);
                periodData.put("total", periodData.get("total") + 1);

                String status = workOrder.getStatus();
                if (WorkOrderStatus.COMPLETED.getCode().equalsIgnoreCase(status)) {
                    periodData.put("completed", periodData.get("completed") + 1);
                } else if (WorkOrderStatus.PENDING.getCode().equalsIgnoreCase(status)) {
                    periodData.put("pending", periodData.get("pending") + 1);
                } else if (WorkOrderStatus.PROCESSING.getCode().equalsIgnoreCase(status)) {
                    periodData.put("processing", periodData.get("processing") + 1);
                } else if (WorkOrderStatus.CLOSED.getCode().equalsIgnoreCase(status)) {
                    periodData.put("closed", periodData.get("closed") + 1);
                }
            }
        }

        return result;
    }

    /**
     * 获取周标识符
     */
    private String getWeekKey(LocalDate date) {
        int week = (date.getDayOfYear() - 1) / 7 + 1;
        return date.getYear() + "-W" + String.format("%02d", week);
    }

    /**
     * 获取月标识符
     */
    private String getMonthKey(LocalDate date) {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
    }
}