package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiChatMessageRepository extends BaseMapper<AiChatMessageEntity> {
    List<AiChatMessageEntity> findBySessionId(@Param("sessionId") Long sessionId);
}