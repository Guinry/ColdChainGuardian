package com.coldchain.guardian.contract.dto.area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BatchAreaOperationRequestDto {

    @NotBlank(message = "操作类型不能为空")
    private String action; // 操作类型：enable/disable/enableAlarm/disableAlarm

    @NotNull(message = "库区ID列表不能为空")
    private List<Long> ids; // 库区ID列表

    // constructors
    public BatchAreaOperationRequestDto() {}

    // getters and setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}