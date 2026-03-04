package com.coldchain.guardian.infra.persistence.repository;

import com.coldchain.guardian.infra.persistence.entity.WorkOrderEntity;
import com.coldchain.guardian.infra.persistence.mapper.WorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WorkOrderRepository {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    public List<WorkOrderEntity> findAll() {
        return workOrderMapper.selectList(null);
    }

    public long count() {
        return workOrderMapper.selectCount(null);
    }

    public WorkOrderEntity findById(Long id) {
        return workOrderMapper.selectById(id);
    }
}