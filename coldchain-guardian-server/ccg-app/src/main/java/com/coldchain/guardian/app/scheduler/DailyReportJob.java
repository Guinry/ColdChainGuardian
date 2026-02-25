package com.coldchain.guardian.app.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyReportJob {

    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void generateDailyReport() {
        // 生成日报逻辑
    }
}