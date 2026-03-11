package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.ManagerService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.user.EmployeeQueryDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员管理控制器
 */
@Tag(name = "管理员管理", description = "处理管理员信息管理相关接口")
@RestController
@RequestMapping("/api/admin/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    /**
     * 分页查询管理员列表
     */
    @Operation(summary = "查询管理员列表", description = "分页查询管理员列表，支持按关键词、状态、微信绑定状态筛选")
    @GetMapping
    public ApiResponse<PageResponse<UserEntity>> getManagerList(
            EmployeeQueryDto queryDto,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        PageResponse<UserEntity> result = managerService.getManagerList(queryDto, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 创建管理员
     */
    @Operation(summary = "创建管理员", description = "创建新的管理员账号")
    @PostMapping
    public ApiResponse<UserEntity> createManager(@RequestBody UserEntity manager) {
        UserEntity result = managerService.createManager(manager);
        return ApiResponse.success(result);
    }

    /**
     * 更新管理员信息
     */
    @Operation(summary = "更新管理员信息", description = "更新管理员基本信息")
    @PutMapping
    public ApiResponse<UserEntity> updateManager(@RequestBody UserEntity manager) {
        UserEntity result = managerService.updateManager(manager);
        return ApiResponse.success(result);
    }

    /**
     * 更新管理员状态
     */
    @Operation(summary = "更新管理员状态", description = "启用或停用管理员账号")
    @PatchMapping("/{userId}/status")
    public ApiResponse<Boolean> updateManagerStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {

        Boolean result = managerService.updateManagerStatus(userId, status);
        return ApiResponse.success(result);
    }

    /**
     * 解绑管理员微信账号
     */
    @Operation(summary = "解绑微信账号", description = "解除管理员与微信账号的绑定关系")
    @DeleteMapping("/{userId}/wechat-binding")
    public ApiResponse<Boolean> unbindManagerWechat(@PathVariable Long userId) {
        Boolean result = managerService.unbindManagerWechat(userId);
        return ApiResponse.success(result);
    }
}