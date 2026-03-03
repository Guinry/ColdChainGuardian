package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderLogEntity;
import com.coldchain.guardian.infra.persistence.mapper.WorkOrderLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WorkOrderLogRepository {

    @Autowired
    private WorkOrderLogMapper workOrderLogMapper;

    /**
     * 保存工单日志
     */
    public void save(WorkOrderLogEntity log) {
        if (log.getId() == null) {
            workOrderLogMapper.insert(log);
        } else {
            workOrderLogMapper.updateById(log);
        }
    }

    /**
     * 根据工单ID获取日志列表
     */
    public List<WorkOrderLogEntity> findByWorkOrderId(Long workOrderId) {
        LambdaQueryWrapper<WorkOrderLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkOrderLogEntity::getWorkOrderId, workOrderId);
        queryWrapper.orderByAsc(WorkOrderLogEntity::getCreateTime);
        return workOrderLogMapper.selectList(queryWrapper);
    }
}