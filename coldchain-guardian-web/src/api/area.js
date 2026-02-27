import { warehouseAreaApi } from '@/utils/api'

export const areaApi = {
  // 获取库区树结构
  getAreaTree() {
    return warehouseAreaApi.getTree()
  },

  // 根据父ID获取子库区列表
  getChildAreasByParentId(parentId) {
    return warehouseAreaApi.getChildren(parentId)
  },

  // 根据ID获取库区详情
  getAreaById(id) {
    return warehouseAreaApi.getById(id)
  },

  // 创建库区
  createArea(data) {
    return warehouseAreaApi.create(data)
  },

  // 更新库区
  updateArea(id, data) {
    return warehouseAreaApi.update(id, data)
  },

  // 删除库区
  deleteArea(id) {
    return warehouseAreaApi.delete(id)
  },

  // 移动库区
  moveArea(id, targetParentId) {
    return warehouseAreaApi.move(id, targetParentId)
  },

  // 批量操作
  batchOperate(data) {
    return warehouseAreaApi.batch(data)
  }
}