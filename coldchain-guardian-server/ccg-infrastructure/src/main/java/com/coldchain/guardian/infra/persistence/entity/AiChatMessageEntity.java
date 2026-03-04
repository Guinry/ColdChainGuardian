package com.coldchain.guardian.infra.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_messages")
public class AiChatMessageEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId; // 所属会话ID

    @TableField("role")
    private String role; // 角色类型：USER, ASSISTANT, SYSTEM

    @TableField("content")
    private String content; // 消息内容

    @TableField("attachment_type")
    private String attachmentType; // 附件类型：DEVICE, ALERT, WORK_ORDER, AREA

    @TableField("attachment_id")
    private Long attachmentId; // 附件的业务ID

    @TableField("tokens_used")
    private Integer tokensUsed; // 消耗的Token数量

    // 构造函数
    public AiChatMessageEntity() {}
}