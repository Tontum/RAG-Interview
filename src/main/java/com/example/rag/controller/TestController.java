package com.example.rag.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class TestController {
    
    private final ChatClient.Builder chatClientBuilder;
    
    /**
     * 非流式输出
     */
    @GetMapping("/test/ai")
    public String testAi(@RequestParam(defaultValue = "你好，请简单介绍一下自己") String message) {
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
    
    /**
     * 流式输出 (SSE)
     */
    @GetMapping(value = "/test/ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> testAiStream(@RequestParam(defaultValue = "你好，请简单介绍一下自己") String message) {
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}