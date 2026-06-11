package com.example.rag.controller;

import com.example.rag.dto.QueryRequest;
import com.example.rag.dto.QueryResponse;
import com.example.rag.service.RagQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * RAG问答Controller
 */
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagChatController {
    
    private final RagQueryService ragQueryService;
    
    /**
     * 非流式查询
     */
    @PostMapping("/query")
    public QueryResponse query(@RequestBody QueryRequest request) {
        String answer = ragQueryService.query(
            request.getKnowledgeBaseIds(), 
            request.getQuestion()
        );
        return new QueryResponse(answer);
    }
    
    /**
     * 流式查询
     */
    @PostMapping(value = "/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> queryStream(@RequestBody QueryRequest request) {
        return ragQueryService.queryStream(
            request.getKnowledgeBaseIds(), 
            request.getQuestion(),
            null // TODO: 支持历史消息
        );
    }
}