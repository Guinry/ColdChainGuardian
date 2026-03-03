package com.coldchain.guardian.app.scheduler;

import com.coldchain.guardian.app.service.AlertService;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警相关的定时任务
 * 包括：清理过期告警、发送告警通知、统计告警数据等
 */
@Component
public class AlertScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AlertScheduler.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    /**
     * 每分钟检查一次紧急告警
     * 如果紧急告警超过一定时间未处理，则发送通知
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void checkUrgentAlerts() {
        logger.info("开始检查紧急告警...");

        // 获取所有未处理的紧急告警
        List<AlertEntity> urgentAlerts = alertRepository.findUrgentAlerts();

        for (AlertEntity alert : urgentAlerts) {
            // 检查是否超过10分钟未处理
            if (alert.getFirstTime().isBefore(LocalDateTime.now().minusMinutes(10))) {
                logger.warn("紧急告警超过10分钟未处理，告警ID: {}, 设备: {}, 类型: {}",
                           alert.getId(), alert.getDeviceName(), alert.getAlertType());
                // 这里可以添加发送通知的逻辑，如邮件、短信、钉钉等
                sendUrgentNotification(alert);
            }
        }

        logger.info("紧急告警检查完成，共发现 {} 个紧急告警", urgentAlerts.size());
    }

    /**
     * 每小时清理过期的已解决告警（保留30天）
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupResolvedAlerts() {
        logger.info("开始清理过期的已解决告警...");

        // 获取超过30天的已解决告警
        // 这里可以添加具体的清理逻辑

        logger.info("告警清理任务完成");
    }

    /**
     * 每天生成告警统计报告
     */
    @Scheduled(cron = "0 0 6 * * ?") // 每天早上6点执行
    public void generateDailyAlertReport() {
        logger.info("开始生成每日告警统计报告...");

        // 获取昨日的告警统计数据
        // 这里可以添加生成报告的逻辑

        logger.info("每日告警统计报告生成完成");
    }

    /**
     * 发送紧急通知
     */
    private void sendUrgentNotification(AlertEntity alert) {
        // 这里可以集成具体的推送机制，如邮件、短信、企业微信等
        logger.info("发送紧急通知: 告警ID={}, 设备={}, 类型={}",
                   alert.getId(), alert.getDeviceName(), alert.getAlertType());
    }
}