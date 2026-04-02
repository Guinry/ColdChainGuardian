package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AreaService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.area.AreaDto;
import com.coldchain.guardian.contract.dto.area.AreaTreeNodeDto;
import com.coldchain.guardian.contract.dto.area.BatchAreaOperationRequestDto;
import com.coldchain.guardian.contract.dto.area.CreateAreaRequestDto;
import com.coldchain.guardian.contract.dto.area.MoveAreaRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "库区管理", description = "处理库区树结构管理、增删改查等操作")
@RestController
@RequestMapping("/api/areas")
public class AreaController {

    @Autowired
    private AreaService areaService;

    /**
     * 获取完整库区树结构
     */
    @Operation(summary = "获取库区树", description = "获取完整的库区树形结构")
    @GetMapping
    public ApiResponse<List<AreaTreeNodeDto>> getAllAreasTree() {
        try {
            List<AreaTreeNodeDto> areas = areaService.getAreaTree();
            return ApiResponse.success(areas);
        } catch (Exception e) {
            return ApiResponse.error("获取库区树失败：" + e.getMessage());
        }
    }

    /**
     * 根据父 ID 获取子库区列表
     */
    @Operation(summary = "获取子库区", description = "根据父库区 ID 获取子库区列表")
    @GetMapping("/parent/{parentId}")
    public ApiResponse<List<AreaDto>> getChildAreasByParentId(@PathVariable Long parentId) {
        try {
            List<AreaDto> areas = areaService.getChildAreasByParentId(parentId);
            return ApiResponse.success(areas);
        } catch (Exception e) {
            return ApiResponse.error("获取子库区失败：" + e.getMessage());
        }
    }

    /**
     * 根据 ID 获取库区信息
     */
    @Operation(summary = "获取库区详情", description = "根据库区 ID 获取详细信息")
    @GetMapping("/{id}")
    public ApiResponse<AreaDto> getAreaById(@PathVariable Long id) {
        try {
            AreaDto area = areaService.getAreaById(id);
            return ApiResponse.success(area);
        } catch (Exception e) {
            return ApiResponse.error("获取库区详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建库区
     */
    @Operation(summary = "创建库区", description = "创建新的库区节点")
    @PostMapping
    public ApiResponse<AreaDto> createArea(@Valid @RequestBody CreateAreaRequestDto requestDto) {
        try {
            AreaDto area = areaService.createArea(requestDto);
            return ApiResponse.success(area);
        } catch (Exception e) {
            return ApiResponse.error("创建库区失败：" + e.getMessage());
        }
    }

    /**
     * 更新库区信息
     */
    @Operation(summary = "更新库区", description = "更新库区的基本信息")
    @PutMapping("/{id}")
    public ApiResponse<AreaDto> updateArea(@PathVariable Long id,
                                         @Valid @RequestBody CreateAreaRequestDto requestDto) {
        try {
            AreaDto area = areaService.updateArea(id, requestDto);
            return ApiResponse.success(area);
        } catch (Exception e) {
            return ApiResponse.error("更新库区失败：" + e.getMessage());
        }
    }

    /**
     * 删除库区
     */
    @Operation(summary = "删除库区", description = "删除指定的库区节点")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArea(@PathVariable Long id) {
        try {
            areaService.deleteArea(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("删除库区失败：" + e.getMessage());
        }
    }

    /**
     * 移动库区
     */
    @Operation(summary = "移动库区", description = "将库区移动到新的父节点下")
    @PostMapping("/{id}/move")
    public ApiResponse<Void> moveArea(@PathVariable Long id, @RequestBody MoveAreaRequestDto requestDto) {
        try {
            areaService.moveArea(id, requestDto.getTargetParentId());
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("移动库区失败：" + e.getMessage());
        }
    }

    /**
     * 批量操作
     */
    @Operation(summary = "批量操作库区", description = "批量删除或启用/禁用库区")
    @PostMapping("/batch")
    public ApiResponse<AreaService.BatchOperationResult> batchOperate(@Valid @RequestBody BatchAreaOperationRequestDto requestDto) {
        try {
            AreaService.BatchOperationResult result = areaService.batchOperate(requestDto.getAction(), requestDto.getIds());
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("批量操作库区失败：" + e.getMessage());
        }
    }
}
