package com.coldchain.guardian.app.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    private final ChatModel chatModel;

    public AIConfig(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel).build();
    }
}