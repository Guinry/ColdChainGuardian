package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.WorkOrderService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.workorder.WorkOrderDto;
import com.coldchain.guardian.contract.dto.workorder.CreateWorkOrderRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 创建工单
     */
    @PostMapping
    public ApiResponse<WorkOrderDto> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequestDto requestDto) {
        WorkOrderDto workOrder = workOrderService.createWorkOrder(requestDto);
        return ApiResponse.success(workOrder);
    }

    /**
     * 获取工单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WorkOrderDto> getWorkOrderById(@PathVariable Long id) {
        WorkOrderDto workOrder = workOrderService.getWorkOrderById(id);
        if (workOrder == null) {
            return ApiResponse.error(404, "工单不存在");
        }
        return ApiResponse.success(workOrder);
    }

    /**
     * 获取工单列表
     */
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
    }

    /**
     * 更新工单状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<WorkOrderDto> updateWorkOrderStatus(@PathVariable Long id,
                                                          @RequestBody UpdateWorkOrderStatusRequest request) {
        WorkOrderDto workOrder = workOrderService.updateWorkOrderStatus(id, request.getStatus(),
                                                                      request.getRemark(), request.getOperatorId(),
                                                                      request.getOperatorName());
        if (workOrder == null) {
            return ApiResponse.error(404, "工单不存在");
        }
        return ApiResponse.success(workOrder);
    }

    /**
     * 更新工单信息
     */
    @PutMapping("/{id}")
    public ApiResponse<WorkOrderDto> updateWorkOrder(@PathVariable Long id,
                                                     @Valid @RequestBody CreateWorkOrderRequestDto requestDto) {
        WorkOrderDto workOrder = workOrderService.updateWorkOrder(id, requestDto);
        if (workOrder == null) {
            return ApiResponse.error(404, "工单不存在");
        }
        return ApiResponse.success(workOrder);
    }

    /**
     * 获取工单统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getWorkOrderStats() {
        Map<String, Object> stats = workOrderService.getWorkOrderStats();
        return ApiResponse.success(stats);
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