package com.coldchain.guardian.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSessionEntity> {
}