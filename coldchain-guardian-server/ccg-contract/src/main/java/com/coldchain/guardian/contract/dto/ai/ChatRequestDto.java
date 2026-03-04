package com.coldchain.guardian.contract.dto.ai;

import lombok.Data;

@Data
public class ChatRequestDto {
    private String message;           // 用户输入的文字
    private String attachmentType;    // 附件类型：DEVICE, ALERT, WORK_ORDER
    private Long attachmentId;        // 附件对应的 ID
    private Long sessionId;           // 会话ID，如果为null则新建会话
}