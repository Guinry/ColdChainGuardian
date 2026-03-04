package com.coldchain.guardian.app.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.qwen.QwenChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    // 这里需要QwenChatModel的配置，但由于项目中可能缺少Spring AI Qwen依赖
    // 实际使用时需要在pom.xml中添加相关依赖

    @Bean
    public ChatClient.Builder chatClientBuilder(QwenChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}