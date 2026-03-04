package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.mapper.AiChatMessageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AiChatMessageRepository extends ServiceImpl<AiChatMessageMapper, AiChatMessageEntity> {

    /**
     * 根据会话ID查找消息
     */
    public List<AiChatMessageEntity> findBySessionId(Long sessionId) {
        return this.lambdaQuery()
                .eq(AiChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(AiChatMessageEntity::getCreateTime)
                .list();
    }

    /**
     * 保存或更新消息
     */
    public void insert(AiChatMessageEntity message) {
        this.save(message);
    }
}