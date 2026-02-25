package com.coldchain.guardian.app.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeviceOfflineDetectionJob {

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void detectOfflineDevices() {
        // 检测离线设备逻辑
    }
}