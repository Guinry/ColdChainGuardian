package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import com.coldchain.guardian.infra.persistence.mapper.AiChatSessionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AiChatSessionRepository extends ServiceImpl<AiChatSessionMapper, AiChatSessionEntity> {

    /**
     * 根据用户ID查找会话
     */
    public List<AiChatSessionEntity> findByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(AiChatSessionEntity::getUserId, userId)
                .orderByDesc(AiChatSessionEntity::getUpdateTime)
                .list();
    }

    /**
     * 保存或更新会话
     */
    public void insert(AiChatSessionEntity session) {
        this.save(session);
    }
}