package com.coldchain.guardian.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.coldchain.guardian")
@MapperScan("com.coldchain.guardian.infra.persistence.mapper")
@EnableScheduling
public class CcgApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcgApplication.class, args);
    }

}