package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AreaService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.area.AreaDto;
import com.coldchain.guardian.contract.dto.area.AreaTreeNodeDto;
import com.coldchain.guardian.contract.dto.area.BatchAreaOperationRequestDto;
import com.coldchain.guardian.contract.dto.area.CreateAreaRequestDto;
import com.coldchain.guardian.contract.dto.area.MoveAreaRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    @Autowired
    private AreaService areaService;

    /**
     * 获取完整库区树结构
     */
    @GetMapping
    public ApiResponse<List<AreaTreeNodeDto>> getAllAreasTree() {
        List<AreaTreeNodeDto> areas = areaService.getAreaTree();
        return ApiResponse.success(areas);
    }

    /**
     * 根据父ID获取子库区列表
     */
    @GetMapping("/parent/{parentId}")
    public ApiResponse<List<AreaDto>> getChildAreasByParentId(@PathVariable Long parentId) {
        List<AreaDto> areas = areaService.getChildAreasByParentId(parentId);
        return ApiResponse.success(areas);
    }

    /**
     * 根据ID获取库区信息
     */
    @GetMapping("/{id}")
    public ApiResponse<AreaDto> getAreaById(@PathVariable Long id) {
        AreaDto area = areaService.getAreaById(id);
        return ApiResponse.success(area);
    }

    /**
     * 创建库区
     */
    @PostMapping
    public ApiResponse<AreaDto> createArea(@Valid @RequestBody CreateAreaRequestDto requestDto) {
        AreaDto area = areaService.createArea(requestDto);
        return ApiResponse.success(area);
    }

    /**
     * 更新库区信息
     */
    @PutMapping("/{id}")
    public ApiResponse<AreaDto> updateArea(@PathVariable Long id,
                                         @Valid @RequestBody CreateAreaRequestDto requestDto) {
        AreaDto area = areaService.updateArea(id, requestDto);
        return ApiResponse.success(area);
    }

    /**
     * 删除库区
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArea(@PathVariable Long id) {
        areaService.deleteArea(id);
        return ApiResponse.success(null);
    }

    /**
     * 移动库区
     */
    @PostMapping("/{id}/move")
    public ApiResponse<Void> moveArea(@PathVariable Long id, @RequestBody MoveAreaRequestDto requestDto) {
        areaService.moveArea(id, requestDto.getTargetParentId());
        return ApiResponse.success(null);
    }

    /**
     * 批量操作
     */
    @PostMapping("/batch")
    public ApiResponse<AreaService.BatchOperationResult> batchOperate(@Valid @RequestBody BatchAreaOperationRequestDto requestDto) {
        AreaService.BatchOperationResult result = areaService.batchOperate(requestDto.getAction(), requestDto.getIds());
        return ApiResponse.success(result);
    }
}