package com.coldchain.guardian.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.guardian.infra.persistence.entity.AreaEntity;
import com.coldchain.guardian.infra.persistence.mapper.AreaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AreaRepository {

    @Autowired
    private AreaMapper areaMapper;

    /**
     * 保存库区
     */
    public void save(AreaEntity area) {
        if (area.getId() == null) {
            areaMapper.insert(area);
        } else {
            areaMapper.updateById(area);
        }
    }

    /**
     * 根据ID查找库区
     */
    public AreaEntity findById(Long id) {
        return areaMapper.selectById(id);
    }

    /**
     * 根据库区编码查找库区
     */
    public AreaEntity findByAreaCode(String areaCode) {
        LambdaQueryWrapper<AreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaEntity::getAreaCode, areaCode);
        return areaMapper.selectOne(queryWrapper);
    }

    /**
     * 根据库区名称查找库区
     */
    public AreaEntity findByAreaName(String areaName) {
        LambdaQueryWrapper<AreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaEntity::getAreaName, areaName);
        return areaMapper.selectOne(queryWrapper);
    }

    /**
     * 查找所有库区
     */
    public List<AreaEntity> findAll() {
        LambdaQueryWrapper<AreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(AreaEntity::getSortNo).orderByDesc(AreaEntity::getId);
        return areaMapper.selectList(queryWrapper);
    }

    /**
     * 查找子库区
     */
    public List<AreaEntity> findChildrenByParentId(Long parentId) {
        LambdaQueryWrapper<AreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaEntity::getParentId, parentId);
        queryWrapper.orderByAsc(AreaEntity::getSortNo).orderByDesc(AreaEntity::getId);
        return areaMapper.selectList(queryWrapper);
    }

    /**
     * 根据ID删除库区
     */
    public void deleteById(Long id) {
        areaMapper.deleteById(id);
    }

    /**
     * 检查库区编码是否存在（排除指定ID的库区）
     */
    public boolean existsByAreaCodeExcludingId(String areaCode, Long excludeId) {
        LambdaQueryWrapper<AreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaEntity::getAreaCode, areaCode);
        if (excludeId != null) {
            queryWrapper.ne(AreaEntity::getId, excludeId);
        }
        return areaMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查库区名称是否存在（排除指定ID的库区）
     */
    public boolean existsByAreaNameExcludingId(String areaName, Long excludeId) {
        LambdaQueryWrapper<AreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaEntity::getAreaName, areaName);
        if (excludeId != null) {
            queryWrapper.ne(AreaEntity::getId, excludeId);
        }
        return areaMapper.selectCount(queryWrapper) > 0;
    }
}