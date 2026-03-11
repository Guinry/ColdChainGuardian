package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.EmployeeService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.user.EmployeeQueryDto;
import com.coldchain.guardian.contract.dto.user.EmployeeUpdateDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理控制器
 */
@Tag(name = "员工管理", description = "处理员工信息管理相关接口")
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 分页查询员工列表
     */
    @Operation(summary = "查询员工列表", description = "分页查询员工列表，支持按关键词、角色、状态、微信绑定状态筛选")
    @GetMapping
    public ApiResponse<PageResponse<UserEntity>> getEmployeeList(
            EmployeeQueryDto queryDto,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        PageResponse<UserEntity> result = employeeService.getEmployeeList(queryDto, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 创建员工
     */
    @Operation(summary = "创建员工", description = "创建新的员工账号（无需设置密码，使用手机号作为登录凭证）")
    @PostMapping
    public ApiResponse<UserEntity> createEmployee(@RequestBody UserEntity employee) {
        UserEntity result = employeeService.createEmployee(employee);
        return ApiResponse.success(result);
    }

    /**
     * 更新员工信息
     */
    @Operation(summary = "更新员工信息", description = "更新员工基本信息")
    @PutMapping
    public ApiResponse<UserEntity> updateEmployee(@RequestBody UserEntity employee) {
        UserEntity result = employeeService.updateEmployee(employee);
        return ApiResponse.success(result);
    }

    /**
     * 更新员工状态
     */
    @Operation(summary = "更新员工状态", description = "启用或停用员工账号（停用后员工将无法登录）")
    @PatchMapping("/{userId}/status")
    public ApiResponse<Boolean> updateEmployeeStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {

        Boolean result = employeeService.updateEmployeeStatus(userId, status);
        return ApiResponse.success(result);
    }

    /**
     * 解绑员工微信账号
     */
    @Operation(summary = "解绑微信账号", description = "解除员工与微信账号的绑定关系")
    @DeleteMapping("/{userId}/wechat-binding")
    public ApiResponse<Boolean> unbindEmployeeWechat(@PathVariable Long userId) {
        Boolean result = employeeService.unbindEmployeeWechat(userId);
        return ApiResponse.success(result);
    }
}