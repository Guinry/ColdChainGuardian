package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.enums.WorkOrderStatus;
import com.coldchain.guardian.contract.dto.dashboard.AreaOverviewDto;
import com.coldchain.guardian.contract.dto.dashboard.PendingOrderDto;
import com.coldchain.guardian.contract.dto.dashboard.RecentAlertDto;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.entity.TelemetryEntity;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderEntity;
import com.coldchain.guardian.infra.persistence.repository.AlertRepository;
import com.coldchain.guardian.infra.persistence.repository.AreaRepository;
import com.coldchain.guardian.infra.persistence.repository.DeviceRepository;
import com.coldchain.guardian.infra.persistence.repository.WorkOrderRepository;
import com.coldchain.guardian.infra.persistence.repository.TelemetryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    // Recommended: Use constructor injection instead of @Autowired on fields
    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final TelemetryRepository telemetryRepository;
    private final AreaRepository areaRepository;

    public DashboardService(AlertRepository alertRepository,
                            DeviceRepository deviceRepository,
                            WorkOrderRepository workOrderRepository,
                            TelemetryRepository telemetryRepository,
                            AreaRepository areaRepository) {
        this.alertRepository = alertRepository;
        this.deviceRepository = deviceRepository;
        this.workOrderRepository = workOrderRepository;
        this.telemetryRepository = telemetryRepository;
        this.areaRepository = areaRepository;
    }

    /**
     * 安全解析日期，处理ISO 8601格式的时间戳
     */
    private LocalDate parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        if (dateStr.contains("T")) {
            dateStr = dateStr.substring(0, 10);
        }
        return LocalDate.parse(dateStr);
    }

    public Map<String, Object> getComprehensiveTrendAnalysis(String startDate, String endDate, String interval, String dimension) {
        Map<String, Object> result = new HashMap<>();
        result.put("environmentTrend", getEnvironmentTrend(startDate, endDate, interval));
        result.put("alertTrend", getAlertTrend(startDate, endDate, interval));
        result.put("workOrderTrend", getWorkOrderTrend(startDate, endDate, interval));
        result.put("deviceStatusTrend", getDeviceStatusTrend(startDate, endDate, interval));

        if (dimension != null && !dimension.isEmpty()) {
            result.put("dimension", dimension);
        }
        return result;
    }

    public Map<String, Object> getEnvironmentTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        List<TelemetryEntity> telemetryData = telemetryRepository.findAll();
        List<TelemetryEntity> filteredData = telemetryData.stream()
                .filter(data -> {
                    if (data.getDataTime() == null) return false;
                    LocalDate dataDate = data.getDataTime().toLocalDate();
                    return !dataDate.isBefore(start) && !dataDate.isAfter(end);
                })
                .toList();

        Map<String, List<TelemetryEntity>> groupedData = filteredData.stream()
                .collect(Collectors.groupingBy(
                        data -> {
                            LocalDate dataDate = data.getDataTime().toLocalDate();
                            return switch (interval.toLowerCase()) {
                                case "weekly" -> getWeekKey(dataDate);
                                case "monthly" -> getMonthKey(dataDate);
                                case "hourly" -> data.getDataTime().truncatedTo(ChronoUnit.HOURS).toString();
                                default -> dataDate.toString();
                            };
                        }
                ));

        List<Map<String, Object>> dataPoints = groupedData.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey();
                    List<TelemetryEntity> periodData = entry.getValue();

                    double avgTemp = periodData.stream().mapToDouble(d -> d.getTemperature() != null ? d.getTemperature() : 0.0).average().orElse(0.0);
                    double avgHumidity = periodData.stream().mapToDouble(d -> d.getHumidity() != null ? d.getHumidity() : 0.0).average().orElse(0.0);
                    double maxTemp = periodData.stream().mapToDouble(d -> d.getTemperature() != null ? d.getTemperature() : 0.0).max().orElse(0.0);
                    double minTemp = periodData.stream().mapToDouble(d -> d.getTemperature() != null ? d.getTemperature() : 0.0).min().orElse(0.0);
                    double maxHumidity = periodData.stream().mapToDouble(d -> d.getHumidity() != null ? d.getHumidity() : 0.0).max().orElse(0.0);
                    double minHumidity = periodData.stream().mapToDouble(d -> d.getHumidity() != null ? d.getHumidity() : 0.0).min().orElse(0.0);

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
                .sorted(Comparator.comparing(m -> m.get("date").toString()))
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("data", dataPoints);
        result.put("interval", interval);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        return result;
    }

    public Map<String, Object> getAlertTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        List<AlertEntity> alerts = alertRepository.findAll();
        List<AlertEntity> filteredAlerts = alerts.stream()
                .filter(alert -> alert.getFirstTime() != null)
                .filter(alert -> {
                    LocalDate alertDate = alert.getFirstTime().toLocalDate();
                    return !alertDate.isBefore(start) && !alertDate.isAfter(end);
                })
                .toList();

        Map<String, Map<String, Long>> alertStats = groupAlertsByInterval(filteredAlerts, start, end, interval);

        Map<String, Object> result = new HashMap<>();
        result.put("data", alertStats);
        result.put("total", (long) filteredAlerts.size());
        result.put("interval", interval);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        return result;
    }

    public Map<String, Object> getWorkOrderTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        List<WorkOrderEntity> workOrders = workOrderRepository.findAll();
        List<WorkOrderEntity> filteredWorkOrders = workOrders.stream()
                .filter(wo -> wo.getCreateTime() != null)
                .filter(wo -> {
                    LocalDate woDate = wo.getCreateTime().toLocalDate();
                    return !woDate.isBefore(start) && !woDate.isAfter(end);
                })
                .toList();

        Map<String, Map<String, Long>> workOrderStats = groupWorkOrdersByInterval(filteredWorkOrders, start, end, interval);

        Map<String, Object> result = new HashMap<>();
        result.put("data", workOrderStats);
        result.put("total", (long) filteredWorkOrders.size());
        result.put("interval", interval);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        return result;
    }

    public Map<String, Object> getDeviceStatusTrend(String startDate, String endDate, String interval) {
        LocalDate start = startDate != null ? parseSafeDate(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? parseSafeDate(endDate) : LocalDate.now();

        List<DeviceEntity> devices = deviceRepository.findAll();
        Map<String, Long> statusCount = devices.stream()
                .collect(Collectors.groupingBy(
                        device -> device.getOnlineStatus() != null && device.getOnlineStatus() == 1 ? "online" : "offline",
                        Collectors.counting()
                ));

        List<Map<String, Object>> historicalData = new ArrayList<>();
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        for (int i = 0; i <= daysBetween; i++) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", start.plusDays(i).toString());
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

    public Map<String, Object> getDashboardStats() {
        List<AlertEntity> alerts = alertRepository.findAll();

        // This is the line that was broken. Fixed by adding a clean null check before mapping to date.
        long todayAlerts = alerts.stream()
                .filter(alert -> alert.getFirstTime() != null)
                .filter(alert -> alert.getFirstTime().toLocalDate().equals(LocalDate.now()))
                .count();

        long unhandledAlerts = alerts.stream()
                .filter(alert -> "UNHANDLED".equals(alert.getStatus()))
                .count();

        List<WorkOrderEntity> workOrders = workOrderRepository.findAll();
        long todayClosedWorkOrders = workOrders.stream()
                .filter(wo -> wo.getUpdateTime() != null)
                .filter(wo -> wo.getUpdateTime().toLocalDate().equals(LocalDate.now())
                        && (WorkOrderStatus.COMPLETED.getCode().equals(wo.getStatus()) ||
                        WorkOrderStatus.CLOSED.getCode().equals(wo.getStatus())))
                .count();

        List<DeviceEntity> devices = deviceRepository.findAll();
        long totalDevices = devices.size();
        long onlineDevices = devices.stream()
                .filter(device -> device.getOnlineStatus() != null && device.getOnlineStatus() == 1)
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("onlineDevices", onlineDevices);
        result.put("totalDevices", totalDevices);
        result.put("todayAlerts", todayAlerts);
        result.put("unhandledAlerts", unhandledAlerts);
        result.put("todayClosedWorkOrders", todayClosedWorkOrders);

        return result;
    }

    private Map<String, Map<String, Long>> groupAlertsByInterval(List<AlertEntity> alerts, LocalDate start, LocalDate end, String interval) {
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();
        long periods = calculatePeriods(start, end, interval);

        for (long i = 0; i < periods; i++) {
            String periodKey = determinePeriodKey(start.plusDays(i), interval);
            result.putIfAbsent(periodKey, createInitialAlertStats());
        }

        for (AlertEntity alert : alerts) {
            LocalDate alertDate = alert.getFirstTime().toLocalDate();
            if (!alertDate.isBefore(start) && !alertDate.isAfter(end)) {
                String periodKey = determinePeriodKey(alertDate, interval);
                if (result.containsKey(periodKey)) {
                    Map<String, Long> periodData = result.get(periodKey);
                    periodData.put("total", periodData.get("total") + 1);
                    incrementAlertLevelCounter(periodData, alert.getAlertLevel());
                }
            }
        }
        return result;
    }

    private void incrementAlertLevelCounter(Map<String, Long> periodData, String level) {
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

    private Map<String, Long> createInitialAlertStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", 0L);
        stats.put("critical", 0L);
        stats.put("high", 0L);
        stats.put("medium", 0L);
        stats.put("low", 0L);
        return stats;
    }

    private Map<String, Map<String, Long>> groupWorkOrdersByInterval(List<WorkOrderEntity> workOrders, LocalDate start, LocalDate end, String interval) {
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();
        long periods = calculatePeriods(start, end, interval);

        for (long i = 0; i < periods; i++) {
            String periodKey = determinePeriodKey(start.plusDays(i), interval);
            result.putIfAbsent(periodKey, createInitialWorkOrderStats());
        }

        for (WorkOrderEntity workOrder : workOrders) {
            if (workOrder.getCreateTime() == null) continue;
            LocalDate woDate = workOrder.getCreateTime().toLocalDate();
            if (!woDate.isBefore(start) && !woDate.isAfter(end)) {
                String periodKey = determinePeriodKey(woDate, interval);
                if (result.containsKey(periodKey)) {
                    Map<String, Long> periodData = result.get(periodKey);
                    periodData.put("total", periodData.get("total") + 1);
                    incrementWorkOrderStatusCounter(periodData, workOrder.getStatus());
                }
            }
        }
        return result;
    }

    private void incrementWorkOrderStatusCounter(Map<String, Long> periodData, String status) {
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

    private Map<String, Long> createInitialWorkOrderStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", 0L);
        stats.put("completed", 0L);
        stats.put("pending", 0L);
        stats.put("processing", 0L);
        stats.put("closed", 0L);
        return stats;
    }

    private long calculatePeriods(LocalDate start, LocalDate end, String interval) {
        return switch (interval.toLowerCase()) {
            case "weekly" -> ChronoUnit.WEEKS.between(start, end) + 1;
            case "monthly" -> ChronoUnit.MONTHS.between(start, end) + 1;
            case "hourly" -> ChronoUnit.HOURS.between(start.atStartOfDay(), end.atStartOfDay()) + 1;
            default -> ChronoUnit.DAYS.between(start, end) + 1;
        };
    }

    private String determinePeriodKey(LocalDate date, String interval) {
        return switch (interval.toLowerCase()) {
            case "weekly" -> getWeekKey(date);
            case "monthly" -> getMonthKey(date);
            case "hourly" -> date.atStartOfDay().toString();
            default -> date.toString();
        };
    }

    private String getWeekKey(LocalDate date) {
        int week = (date.getDayOfYear() - 1) / 7 + 1;
        return date.getYear() + "-W" + String.format("%02d", week);
    }

    private String getMonthKey(LocalDate date) {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
    }

    public List<AreaOverviewDto> getAreaOverview(String timeWindow) {
        List<AreaOverviewDto> areas = new ArrayList<>();
        List<DeviceEntity> allDevices = deviceRepository.findAll();
        List<TelemetryEntity> latestTelemetry = telemetryRepository.findAll();

        // This should theoretically map dynamically to areas in your DB.
        // Using static IDs here based on your existing setup.
        areas.add(createAreaOverview(1L, "库区A", allDevices, latestTelemetry));
        areas.add(createAreaOverview(2L, "库区B", allDevices, latestTelemetry));
        areas.add(createAreaOverview(3L, "库区C", allDevices, latestTelemetry));
        areas.add(createAreaOverview(4L, "库区D", allDevices, latestTelemetry));
        return areas;
    }

    private AreaOverviewDto createAreaOverview(Long id, String name, List<DeviceEntity> devices, List<TelemetryEntity> telemetry) {
        List<DeviceEntity> areaDevices = devices.stream()
                .filter(device -> device.getAreaId() != null && device.getAreaId().equals(id))
                .toList();

        Map<Long, TelemetryEntity> latestTelemetryMap = new HashMap<>();
        for (TelemetryEntity t : telemetry) {
            if (t.getDeviceId() != null && t.getDataTime() != null) {
                TelemetryEntity existing = latestTelemetryMap.get(t.getDeviceId());
                if (existing == null || existing.getDataTime() == null || t.getDataTime().isAfter(existing.getDataTime())) {
                    latestTelemetryMap.put(t.getDeviceId(), t);
                }
            }
        }

        List<Double> temperatures = new ArrayList<>();
        List<Double> humidities = new ArrayList<>();
        int onlineDevices = 0;

        for (DeviceEntity device : areaDevices) {
            if (device.getOnlineStatus() != null && device.getOnlineStatus() == 1) {
                onlineDevices++;
            }
            TelemetryEntity deviceTelemetry = latestTelemetryMap.get(device.getId());
            if (deviceTelemetry != null) {
                if (deviceTelemetry.getTemperature() != null) temperatures.add(deviceTelemetry.getTemperature());
                if (deviceTelemetry.getHumidity() != null) humidities.add(deviceTelemetry.getHumidity());
            }
        }

        double avgTemp = temperatures.isEmpty() ? 0.0 : temperatures.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avgHumidity = humidities.isEmpty() ? 0.0 : humidities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        String status = "normal";
        String statusText = "正常";
        if (avgTemp > 8 || avgTemp < -5) {
            status = "error";
            statusText = "异常";
        } else if (avgTemp > 6 || avgTemp < -2) {
            status = "warning";
            statusText = "警告";
        }

        return new AreaOverviewDto(
                id, name,
                Math.round(avgTemp * 100.0) / 100.0,
                Math.round(avgHumidity * 100.0) / 100.0,
                status, statusText,
                onlineDevices, areaDevices.size()
        );
    }

    public List<RecentAlertDto> getRecentAlerts(int limit) {
        return alertRepository.findAll().stream()
                .filter(alert -> alert.getFirstTime() != null)
                .sorted(Comparator.comparing(AlertEntity::getFirstTime).reversed())
                .limit(limit)
                .map(this::convertToRecentAlertDto)
                .toList();
    }

    public List<PendingOrderDto> getPendingOrders(int limit) {
        return workOrderRepository.findAll().stream()
                .filter(wo -> WorkOrderStatus.PENDING.getCode().equals(wo.getStatus()) ||
                        WorkOrderStatus.PROCESSING.getCode().equals(wo.getStatus()))
                .filter(wo -> wo.getUpdateTime() != null)
                .sorted(Comparator.comparing(WorkOrderEntity::getUpdateTime).reversed())
                .limit(limit)
                .map(this::convertToPendingOrderDto)
                .toList();
    }

    private RecentAlertDto convertToRecentAlertDto(AlertEntity alert) {
        String status = alert.getStatus();
        String displayStatus = "已处理";
        if ("UNHANDLED".equals(status)) {
            displayStatus = "未处理";
        } else if ("HANDLING".equals(status)) {
            displayStatus = "处理中";
        }

        return new RecentAlertDto(
                alert.getId(),
                alert.getFirstTime() != null ? alert.getFirstTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                alert.getAreaName(),
                alert.getDeviceName(),
                alert.getAlertType(),
                alert.getAlertLevel(),
                displayStatus
        );
    }

    private PendingOrderDto convertToPendingOrderDto(WorkOrderEntity order) {
        String displayStatus = "已完成";
        if (WorkOrderStatus.PENDING.getCode().equals(order.getStatus())) {
            displayStatus = "待处理";
        } else if (WorkOrderStatus.PROCESSING.getCode().equals(order.getStatus())) {
            displayStatus = "处理中";
        }

        return new PendingOrderDto(
                order.getOrderNo(),
                order.getDescription(),
                order.getAssigneeName(),
                displayStatus,
                order.getUpdateTime() != null ? order.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""
        );
    }
}