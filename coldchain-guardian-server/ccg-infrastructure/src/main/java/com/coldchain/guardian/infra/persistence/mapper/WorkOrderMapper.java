package com.coldchain.guardian.infra.persistence.mapper;

import com.coldchain.guardian.infra.persistence.entity.WorkOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

public interface WorkOrderMapper extends BaseMapper<WorkOrderEntity> {

    /**
     * 根据状态统计工单数量
     */
    @Select("SELECT COUNT(*) FROM work_orders WHERE status = #{status}")
    long countWorkOrdersByStatus(@Param("status") String status);

    /**
     * 统计逾期工单数量（截止日期已过且状态不是已完成或已关闭）
     */
    @Select("SELECT COUNT(*) FROM work_orders WHERE due_time IS NOT NULL AND due_time < NOW() AND status NOT IN ('COMPLETED', 'CLOSED')")
    long countOverdueWorkOrders();

    /**
     * 统计本周内完成的工单数量
     */
    @Select("SELECT COUNT(*) FROM work_orders WHERE completed_time BETWEEN #{startOfWeek} AND #{endOfWeek} AND status = 'COMPLETED'")
    long countCompletedThisWeek(@Param("startOfWeek") LocalDateTime startOfWeek,
                                @Param("endOfWeek") LocalDateTime endOfWeek);
}