package com.coldchain.guardian.infra.persistence.repository;

import com.coldchain.guardian.infra.persistence.entity.WorkOrderLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class WorkOrderLogRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(WorkOrderLogEntity log) {
        Long operatorId = log.getOperatorId() == null ? 0L : log.getOperatorId();
        LocalDateTime createTime = log.getCreateTime() == null ? LocalDateTime.now() : log.getCreateTime();

        if (log.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO work_order_logs (work_order_id, operator_id, action, remark, created_time) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    log.getWorkOrderId(),
                    operatorId,
                    log.getAction(),
                    log.getRemark(),
                    createTime
            );
            return;
        }

        jdbcTemplate.update(
                "UPDATE work_order_logs SET operator_id = ?, action = ?, remark = ? WHERE id = ?",
                operatorId,
                log.getAction(),
                log.getRemark(),
                log.getId()
        );
    }

    public List<WorkOrderLogEntity> findByWorkOrderId(Long workOrderId) {
        return jdbcTemplate.query(
                "SELECT id, work_order_id, operator_id, action, remark, created_time " +
                        "FROM work_order_logs WHERE work_order_id = ? ORDER BY created_time ASC, id ASC",
                new WorkOrderLogRowMapper(),
                workOrderId
        );
    }

    private static class WorkOrderLogRowMapper implements RowMapper<WorkOrderLogEntity> {
        @Override
        public WorkOrderLogEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            WorkOrderLogEntity log = new WorkOrderLogEntity();
            log.setId(rs.getLong("id"));
            log.setWorkOrderId(rs.getLong("work_order_id"));

            long operatorId = rs.getLong("operator_id");
            log.setOperatorId(rs.wasNull() ? null : operatorId);
            log.setOperatorName(operatorId > 0 ? "操作员 " + operatorId : "系统");

            log.setAction(rs.getString("action"));
            log.setRemark(rs.getString("remark"));

            Timestamp createdTime = rs.getTimestamp("created_time");
            if (createdTime != null) {
                log.setCreateTime(createdTime.toLocalDateTime());
            }
            return log;
        }
    }
}
