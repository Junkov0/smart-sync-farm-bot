package com.junyoung.moddi.smartfarmbot.controller;

import com.junyoung.moddi.smartfarmbot.dto.ChatRequest;
import com.junyoung.moddi.smartfarmbot.dto.ChatResponse;
import com.junyoung.moddi.smartfarmbot.llm.GeminiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiChatService geminiChatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return new ChatResponse(geminiChatService.chat(request.message()));
    }
}
