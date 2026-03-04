package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AIAssistantService;
import com.coldchain.guardian.contract.dto.ai.ChatRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai-assistant")
public class AIAssistantController {

    @Autowired
    private AIAssistantService aiAssistantService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequestDto request) {
        return aiAssistantService.streamChat(request);
    }

    @GetMapping("/sessions/{userId}")
    public List<AiChatSessionEntity> getChatSessions(@PathVariable Long userId) {
        return aiAssistantService.getChatHistory(userId);
    }

    @GetMapping("/messages/{sessionId}")
    public List<AiChatMessageEntity> getChatMessages(@PathVariable Long sessionId) {
        return aiAssistantService.getChatMessagesBySessionId(sessionId);
    }

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody ChatRequestDto request) {
        return aiAssistantService.streamChat(request);
    }
}