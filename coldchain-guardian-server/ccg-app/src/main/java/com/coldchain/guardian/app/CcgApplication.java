package com.coldchain.guardian.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.coldchain.guardian.infra.persistence.mapper")
@ComponentScan(basePackages = {
        "com.coldchain.guardian.app",
        "com.coldchain.guardian.infra",
        "com.coldchain.guardian.common"
})
public class CcgApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcgApplication.class, args);
    }

}