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

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody ChatRequestDto request) {
        return aiAssistantService.streamChat(request);
    }

    @GetMapping("/sessions/{userId}")
    public List<AiChatSessionEntity> getChatSessions(@PathVariable Long userId) {
        return aiAssistantService.getSessionHistory(userId);
    }

    @GetMapping("/messages/{sessionId}")
    public List<AiChatMessageEntity> getChatMessages(@PathVariable Long sessionId) {
        return aiAssistantService.getChatMessagesBySessionId(sessionId);
    }

    @PostMapping("/sessions")
    public AiChatSessionEntity createChatSession(@RequestBody AiChatSessionEntity session) {
        aiAssistantService.createSession(session);
        return session;
    }

    @PutMapping("/sessions/{id}")
    public AiChatSessionEntity updateChatSession(@PathVariable Long id, @RequestBody AiChatSessionEntity session) {
        session.setId(id);
        aiAssistantService.updateSession(session);
        return session;
    }

    @DeleteMapping("/sessions/{id}")
    public void deleteChatSession(@PathVariable Long id) {
        aiAssistantService.deleteSession(id);
    }
}