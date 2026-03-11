package com.coldchain.guardian.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 微信相关配置
 */
@Configuration
public class WxConfig {

    @Value("${wx.miniprogram.request-timeout:5000}")
    private int requestTimeout;

    @Bean
    public RestTemplate wxRestTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置JSON转换器
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        restTemplate.getMessageConverters().add(converter);

        return restTemplate;
    }
}