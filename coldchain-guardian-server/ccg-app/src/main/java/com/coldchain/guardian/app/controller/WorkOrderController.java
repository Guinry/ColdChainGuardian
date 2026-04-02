package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.WorkOrderService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.workorder.WorkOrderDto;
import com.coldchain.guardian.contract.dto.workorder.CreateWorkOrderRequestDto;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderLogEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Tag(name = "工单管理", description = "处理工单创建、查询、状态变更等操作")
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 创建工单
     */
    @Operation(summary = "创建工单", description = "创建新的工单，可关联告警")
    @PostMapping
    public ApiResponse<WorkOrderDto> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequestDto requestDto) {
        try {
            WorkOrderDto workOrder = workOrderService.createWorkOrder(requestDto);
            return ApiResponse.success(workOrder);
        } catch (Exception e) {
            return ApiResponse.error("创建工单失败：" + e.getMessage());
        }
    }

    /**
     * 获取工单详情
     */
    @Operation(summary = "获取工单详情", description = "根据工单 ID 获取工单详细信息")
    @GetMapping("/{id}")
    public ApiResponse<WorkOrderDto> getWorkOrderById(@PathVariable Long id) {
        try {
            WorkOrderDto workOrder = workOrderService.getWorkOrderById(id);
            if (workOrder == null) {
                return ApiResponse.error(404, "工单不存在");
            }
            return ApiResponse.success(workOrder);
        } catch (Exception e) {
            return ApiResponse.error("获取工单详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取工单列表
     */
    @Operation(summary = "获取工单列表", description = "分页查询工单列表，支持多条件筛选")
    @GetMapping
    public ApiResponse<PageResponse<WorkOrderDto>> getWorkOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String workType,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // Get all work orders that match the filters
            List<WorkOrderDto> allWorkOrders = workOrderService.getWorkOrders(status, priority, workType, assigneeId, reporterId, keyword);

            // Calculate pagination
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, allWorkOrders.size());

            List<WorkOrderDto> pagedWorkOrders = startIndex < allWorkOrders.size()
                ? allWorkOrders.subList(startIndex, endIndex)
                : new ArrayList<>();

            // Count total work orders that match the filters
            long total = workOrderService.countWorkOrders(status, priority, workType, assigneeId, reporterId, keyword);

            PageResponse<WorkOrderDto> pageResponse = new PageResponse<>(pagedWorkOrders, total, page, size);
            return ApiResponse.success(pageResponse);
        } catch (Exception e) {
            return ApiResponse.error("获取工单列表失败：" + e.getMessage());
        }
    }

    /**
     * 更新工单状态
     */
    @Operation(summary = "更新工单状态", description = "更新工单的处理状态，自动记录操作日志")
    @PutMapping("/{id}/status")
    public ApiResponse<WorkOrderDto> updateWorkOrderStatus(@PathVariable Long id,
                                                          @RequestBody UpdateWorkOrderStatusRequest request) {
        try {
            WorkOrderDto workOrder = workOrderService.updateWorkOrderStatus(id, request.getStatus(),
                                                                          request.getRemark(), request.getOperatorId(),
                                                                          request.getOperatorName());
            if (workOrder == null) {
                return ApiResponse.error(404, "工单不存在");
            }
            return ApiResponse.success(workOrder);
        } catch (Exception e) {
            return ApiResponse.error("更新工单状态失败：" + e.getMessage());
        }
    }

    /**
     * 更新工单信息
     */
    @Operation(summary = "更新工单信息", description = "更新工单的基本信息（标题、描述、优先级等）")
    @PutMapping("/{id}")
    public ApiResponse<WorkOrderDto> updateWorkOrder(@PathVariable Long id,
                                                     @Valid @RequestBody CreateWorkOrderRequestDto requestDto) {
        try {
            WorkOrderDto workOrder = workOrderService.updateWorkOrder(id, requestDto);
            if (workOrder == null) {
                return ApiResponse.error(404, "工单不存在");
            }
            return ApiResponse.success(workOrder);
        } catch (Exception e) {
            return ApiResponse.error("更新工单信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取工单统计数据
     */
    @Operation(summary = "获取工单统计", description = "获取工单统计数据，包括逾期、待处理、处理中、本周完成等数量")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getWorkOrderStats() {
        try {
            Map<String, Object> stats = workOrderService.getWorkOrderStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取工单统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取工单日志
     */
    @Operation(summary = "获取工单日志", description = "获取指定工单的所有操作日志记录")
    @GetMapping("/{id}/logs")
    public ApiResponse<List<WorkOrderLogEntity>> getWorkOrderLogs(@PathVariable Long id) {
        try {
            List<WorkOrderLogEntity> logs = workOrderService.getWorkOrderLogs(id);
            return ApiResponse.success(logs);
        } catch (Exception e) {
            return ApiResponse.error("获取工单日志失败：" + e.getMessage());
        }
    }

    // Request DTO for updating work order status
    public static class UpdateWorkOrderStatusRequest {
        private String status;
        private String remark;
        private Long operatorId;
        private String operatorName;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public String getOperatorName() {
            return operatorName;
        }

        public void setOperatorName(String operatorName) {
            this.operatorName = operatorName;
        }
    }
}