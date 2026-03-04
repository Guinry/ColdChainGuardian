package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiChatSessionRepository extends BaseMapper<AiChatSessionEntity> {
    List<AiChatSessionEntity> findByUserId(@Param("userId") Long userId);
}