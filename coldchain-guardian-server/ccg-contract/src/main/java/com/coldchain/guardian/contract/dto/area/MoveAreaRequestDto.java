package com.coldchain.guardian.contract.dto.area;

public class MoveAreaRequestDto {

    private Long targetParentId;  // 目标父库区ID

    // constructors
    public MoveAreaRequestDto() {}

    // getters and setters
    public Long getTargetParentId() {
        return targetParentId;
    }

    public void setTargetParentId(Long targetParentId) {
        this.targetParentId = targetParentId;
    }
}