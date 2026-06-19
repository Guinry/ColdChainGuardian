package com.coldchain.guardian.app.service;

import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;
import com.coldchain.guardian.contract.dto.area.AreaDto;
import com.coldchain.guardian.contract.dto.area.AreaTreeNodeDto;
import com.coldchain.guardian.contract.dto.area.CreateAreaRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AreaEntity;
import com.coldchain.guardian.infra.persistence.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AreaService {

    @Autowired
    private AreaRepository areaRepository;

    /**
     * 获取所有库区列表
     */
    public List<AreaDto> getAllAreas() {
        List<AreaEntity> entities = areaRepository.findAll();
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取库区信息
     */
    public AreaDto getAreaById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        AreaEntity entity = areaRepository.findById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.AREA_NOT_EXISTS);
        }

        return convertToDto(entity);
    }

    /**
     * 根据父ID获取子库区列表
     */
    public List<AreaDto> getChildAreasByParentId(Long parentId) {
        List<AreaEntity> entities = areaRepository.findChildrenByParentId(parentId);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 创建库区
     */
    public AreaDto createArea(CreateAreaRequestDto requestDto) {
        // 检查库区编码是否已存在
        if (areaRepository.findByAreaCode(requestDto.getAreaCode()) != null) {
            throw new BusinessException(ErrorCode.AREA_CODE_EXISTS);
        }

        // 创建实体
        AreaEntity entity = new AreaEntity();
        entity.setParentId(requestDto.getParentId());
        entity.setAreaCode(requestDto.getAreaCode());
        entity.setAreaName(requestDto.getAreaName());
        entity.setAreaLevel(requestDto.getAreaLevel());
        entity.setAddress(requestDto.getAddress());
        entity.setLocationDesc(requestDto.getLocationDesc());
        entity.setTemperatureThresholdMin(requestDto.getTemperatureThresholdMin());
        entity.setTemperatureThresholdMax(requestDto.getTemperatureThresholdMax());
        entity.setHumidityThresholdMin(requestDto.getHumidityThresholdMin());
        entity.setHumidityThresholdMax(requestDto.getHumidityThresholdMax());
        entity.setAlarmEnabled(requestDto.getAlarmEnabled());
        entity.setStatus(requestDto.getStatus());
        entity.setSortNo(requestDto.getSortNo());
        entity.setRemark(requestDto.getRemark());

        // 保存到数据库
        areaRepository.save(entity);

        // 返回DTO
        return convertToDto(entity);
    }

    /**
     * 更新库区信息
     */
    public AreaDto updateArea(Long id, CreateAreaRequestDto requestDto) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        // 检查库区是否存在
        AreaEntity existingEntity = areaRepository.findById(id);
        if (existingEntity == null) {
            throw new BusinessException(ErrorCode.AREA_NOT_EXISTS);
        }

        // 检查编码是否与其他库区冲突（排除自身）
        if (areaRepository.existsByAreaCodeExcludingId(requestDto.getAreaCode(), id)) {
            throw new BusinessException(ErrorCode.AREA_CODE_EXISTS);
        }

        // 更新实体
        existingEntity.setParentId(requestDto.getParentId());
        existingEntity.setAreaCode(requestDto.getAreaCode());
        existingEntity.setAreaName(requestDto.getAreaName());
        existingEntity.setAreaLevel(requestDto.getAreaLevel());
        existingEntity.setAddress(requestDto.getAddress());
        existingEntity.setLocationDesc(requestDto.getLocationDesc());
        existingEntity.setTemperatureThresholdMin(requestDto.getTemperatureThresholdMin());
        existingEntity.setTemperatureThresholdMax(requestDto.getTemperatureThresholdMax());
        existingEntity.setHumidityThresholdMin(requestDto.getHumidityThresholdMin());
        existingEntity.setHumidityThresholdMax(requestDto.getHumidityThresholdMax());
        existingEntity.setAlarmEnabled(requestDto.getAlarmEnabled());
        existingEntity.setStatus(requestDto.getStatus());
        existingEntity.setSortNo(requestDto.getSortNo());
        existingEntity.setRemark(requestDto.getRemark());

        // 更新到数据库
        areaRepository.save(existingEntity);

        // 返回DTO
        return convertToDto(existingEntity);
    }

    /**
     * 删除库区
     */
    public void deleteArea(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        // 检查库区是否存在
        AreaEntity entity = areaRepository.findById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.AREA_NOT_EXISTS);
        }

        // 检查是否有子库区
        List<AreaEntity> children = areaRepository.findChildrenByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.AREA_HAS_CHILDREN);
        }

        // 删除库区
        areaRepository.deleteById(id);
    }

    /**
     * 将实体转换为DTO
     */
    private AreaDto convertToDto(AreaEntity entity) {
        AreaDto dto = new AreaDto();
        dto.setId(entity.getId());
        dto.setParentId(entity.getParentId());
        dto.setAreaCode(entity.getAreaCode());
        dto.setAreaName(entity.getAreaName());
        dto.setAreaLevel(entity.getAreaLevel());
        dto.setAddress(entity.getAddress());
        dto.setLocationDesc(entity.getLocationDesc());
        dto.setTemperatureThresholdMin(entity.getTemperatureThresholdMin());
        dto.setTemperatureThresholdMax(entity.getTemperatureThresholdMax());
        dto.setHumidityThresholdMin(entity.getHumidityThresholdMin());
        dto.setHumidityThresholdMax(entity.getHumidityThresholdMax());
        dto.setAlarmEnabled(entity.getAlarmEnabled());
        dto.setStatus(entity.getStatus());
        dto.setSortNo(entity.getSortNo());
        dto.setRemark(entity.getRemark());
        dto.setCreatorId(entity.getCreatorId());
        dto.setUpdaterId(entity.getUpdaterId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    /**
     * 获取完整的库区树结构
     */
    public List<AreaTreeNodeDto> getAreaTree() {
        List<AreaEntity> allEntities = areaRepository.findAll();
        List<AreaDto> allAreas = allEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 创建Map以便快速查找
        Map<Long, AreaTreeNodeDto> nodeMap = new HashMap<>();
        List<AreaTreeNodeDto> rootNodes = new ArrayList<>();

        // 首先将所有DTO转换为TreeNodeDto
        for (AreaDto area : allAreas) {
            AreaTreeNodeDto node = convertToTreeNodeDto(area);
            nodeMap.put(node.getId(), node);
        }

        // 构建树结构
        for (AreaTreeNodeDto node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId <= 0 || !nodeMap.containsKey(parentId)) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 查找父节点并添加到其子节点列表
                AreaTreeNodeDto parentNode = nodeMap.get(parentId);
                parentNode.getChildren().add(node);
            }
        }

        // 对根节点进行排序
        rootNodes.sort((a, b) -> {
            int sortCompare = Integer.compare(normalizeSortNo(a.getSortNo()), normalizeSortNo(b.getSortNo()));
            if (sortCompare != 0) {
                return sortCompare;
            }
            return Long.compare(b.getId(), a.getId()); // ID倒序作为次级排序
        });

        // 递归排序子节点
        for (AreaTreeNodeDto node : rootNodes) {
            sortTreeChildrenRecursively(node);
        }

        return rootNodes;
    }

    /**
     * 递归排序树的所有子节点
     */
    private void sortTreeChildrenRecursively(AreaTreeNodeDto node) {
        if (node.getChildren() != null) {
            node.getChildren().sort((a, b) -> {
                int sortCompare = Integer.compare(normalizeSortNo(a.getSortNo()), normalizeSortNo(b.getSortNo()));
                if (sortCompare != 0) {
                    return sortCompare;
                }
                return Long.compare(b.getId(), a.getId()); // ID倒序作为次级排序
            });

            for (AreaTreeNodeDto child : node.getChildren()) {
                sortTreeChildrenRecursively(child);
            }
        }
    }

    /**
     * 将AreaDto转换为AreaTreeNodeDto
     */
    private AreaTreeNodeDto convertToTreeNodeDto(AreaDto areaDto) {
        AreaTreeNodeDto treeNode = new AreaTreeNodeDto();
        treeNode.setId(areaDto.getId());
        treeNode.setParentId(areaDto.getParentId());
        treeNode.setAreaCode(areaDto.getAreaCode());
        treeNode.setAreaName(areaDto.getAreaName());
        treeNode.setAreaLevel(areaDto.getAreaLevel());
        treeNode.setAddress(areaDto.getAddress());
        treeNode.setLocationDesc(areaDto.getLocationDesc());
        treeNode.setTemperatureThresholdMin(areaDto.getTemperatureThresholdMin());
        treeNode.setTemperatureThresholdMax(areaDto.getTemperatureThresholdMax());
        treeNode.setHumidityThresholdMin(areaDto.getHumidityThresholdMin());
        treeNode.setHumidityThresholdMax(areaDto.getHumidityThresholdMax());
        treeNode.setAlarmEnabled(areaDto.getAlarmEnabled());
        treeNode.setStatus(areaDto.getStatus());
        treeNode.setSortNo(areaDto.getSortNo());
        treeNode.setRemark(areaDto.getRemark());
        treeNode.setCreatorId(areaDto.getCreatorId());
        treeNode.setUpdaterId(areaDto.getUpdaterId());
        treeNode.setCreateTime(areaDto.getCreateTime());
        treeNode.setUpdateTime(areaDto.getUpdateTime());
        treeNode.setChildren(new ArrayList<>());
        return treeNode;
    }

    private int normalizeSortNo(Integer sortNo) {
        return sortNo == null ? Integer.MAX_VALUE : sortNo;
    }

    /**
     * 移动库区到另一个父库区下
     */
    @Transactional
    public void moveArea(Long id, Long targetParentId) {
        if (id == null || targetParentId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        // 检查要移动的库区是否存在
        AreaEntity movingEntity = areaRepository.findById(id);
        if (movingEntity == null) {
            throw new BusinessException(ErrorCode.AREA_NOT_EXISTS);
        }

        // 检查目标父库区是否存在
        if (targetParentId != null) {
            AreaEntity targetParent = areaRepository.findById(targetParentId);
            if (targetParent == null && targetParentId != -1L) { // -1表示顶级节点
                throw new BusinessException(ErrorCode.AREA_NOT_EXISTS);
            }
        }

        // 防止将库区移动到自己或自己的子库区下
        if (isDescendantOf(id, targetParentId)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        // 更新父ID
        movingEntity.setParentId(targetParentId.equals(-1L) ? null : targetParentId);
        areaRepository.save(movingEntity);
    }

    /**
     * 检查sourceId是否是targetId的后代
     */
    private boolean isDescendantOf(Long sourceId, Long targetId) {
        if (targetId == null || targetId.equals(-1L)) {
            return false; // 顶级节点不是任何节点的后代
        }

        AreaEntity current = areaRepository.findById(sourceId);
        while (current != null && current.getParentId() != null) {
            if (current.getParentId().equals(targetId)) {
                return true;
            }
            current = areaRepository.findById(current.getParentId());
        }
        return false;
    }

    /**
     * 批量操作
     */
    @Transactional
    public BatchOperationResult batchOperate(String action, List<Long> ids) {
        if (action == null || ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }

        List<Long> processedIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();

        for (Long id : ids) {
            try {
                AreaEntity entity = areaRepository.findById(id);
                if (entity == null) {
                    failedIds.add(id);
                    continue;
                }

                switch (action.toLowerCase()) {
                    case "enable":
                        entity.setStatus(1);
                        break;
                    case "disable":
                        entity.setStatus(0);
                        break;
                    case "enablealarm":
                        entity.setAlarmEnabled(1);
                        break;
                    case "disablealarm":
                        entity.setAlarmEnabled(0);
                        break;
                    default:
                        throw new BusinessException(ErrorCode.PARAMETER_ERROR);
                }

                areaRepository.save(entity);
                processedIds.add(id);
            } catch (Exception e) {
                failedIds.add(id);
            }
        }

        return new BatchOperationResult(processedIds.size(), processedIds, failedIds);
    }

    /**
     * 批量操作结果
     */
    public static class BatchOperationResult {
        private int processedCount;
        private List<Long> successIds;
        private List<Long> failedIds;

        public BatchOperationResult(int processedCount, List<Long> successIds, List<Long> failedIds) {
            this.processedCount = processedCount;
            this.successIds = successIds;
            this.failedIds = failedIds;
        }

        // getters and setters
        public int getProcessedCount() {
            return processedCount;
        }

        public void setProcessedCount(int processedCount) {
            this.processedCount = processedCount;
        }

        public List<Long> getSuccessIds() {
            return successIds;
        }

        public void setSuccessIds(List<Long> successIds) {
            this.successIds = successIds;
        }

        public List<Long> getFailedIds() {
            return failedIds;
        }

        public void setFailedIds(List<Long> failedIds) {
            this.failedIds = failedIds;
        }
    }
}
