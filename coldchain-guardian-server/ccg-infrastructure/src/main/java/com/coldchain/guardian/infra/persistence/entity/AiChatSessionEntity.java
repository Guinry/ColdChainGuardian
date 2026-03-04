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
@TableName("ai_chat_sessions")
public class AiChatSessionEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId; // 所属用户ID

    @TableField("title")
    private String title; // 会话标题

    @TableField("is_deleted")
    private Integer isDeleted; // 是否已删除(软删除)

    @TableField(value = "user_name", exist = false)
    private String userName; // 用户姓名 (关联查询字段)

    // 构造函数
    public AiChatSessionEntity() {}
}