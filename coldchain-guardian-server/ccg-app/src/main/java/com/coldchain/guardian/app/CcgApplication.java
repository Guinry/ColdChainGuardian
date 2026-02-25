package com.coldchain.guardian.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
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