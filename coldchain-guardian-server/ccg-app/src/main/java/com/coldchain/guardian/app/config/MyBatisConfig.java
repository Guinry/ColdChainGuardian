package com.coldchain.guardian.app.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.coldchain.guardian.infra.persistence.mapper")
public class MyBatisConfig {

}