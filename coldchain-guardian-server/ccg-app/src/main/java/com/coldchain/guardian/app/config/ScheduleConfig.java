package com.coldchain.guardian.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling  // 启用定时任务
@EnableAsync       // 启用异步任务
public class ScheduleConfig {
    // 定时任务配置类
}