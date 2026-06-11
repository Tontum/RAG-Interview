package com.example.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG查询服务
 */
@Slf4j
@Service
public class RagQueryService {
    
    private final ChatClient chatClient;
    private final VectorService vectorService;
    
    private static final String SYSTEM_PROMPT = """
        你是一个专业的知识库问答助手。
        
        规则：
        1. 只根据提供的上下文回答问题
        2. 如果上下文没有相关信息，回答"抱歉，我无法根据提供的信息回答这个问题"
        3. 回答要简洁准确
        4. 不要编造信息
        """;
    
    private static final String USER_PROMPT_TEMPLATE = """
        上下文：
        %s
        
        问题：
        %s
        """;
    
    public RagQueryService(ChatClient.Builder chatClientBuilder, VectorService vectorService) {
        this.chatClient = chatClientBuilder.build();
        this.vectorService = vectorService;
    }
    
    /**
     * RAG 查询（非流式）
     */
    public String query(List<Long> knowledgeBaseIds, String question) {
        log.info("RAG 查询: kbIds={}, question={}", knowledgeBaseIds, question);
        
        // 1. 向量检索
        List<Document> relevantDocs = vectorService.similaritySearch(
            question, knowledgeBaseIds, 8, 0.28
        );
        
        if (relevantDocs.isEmpty()) {
            return "抱歉，在知识库中未找到相关信息。";
        }
        
        // 2. 构建上下文
        String context = relevantDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));
        
        // 3. 构建 Prompt
        String userPrompt = String.format(USER_PROMPT_TEMPLATE, context, question);
        
        // 4. 调用 LLM
        String answer = chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user(userPrompt)
            .call()
            .content();
        
        log.info("RAG 查询完成");
        return answer;
    }
    
    /**
     * RAG 查询（流式）
     */
    public Flux<String> queryStream(List<Long> knowledgeBaseIds, String question, 
                                     List<Message> history) {
        log.info("RAG 流式查询: kbIds={}, question={}", knowledgeBaseIds, question);
        
        // 1. 向量检索
        List<Document> relevantDocs = vectorService.similaritySearch(
            question, knowledgeBaseIds, 8, 0.28
        );
        
        if (relevantDocs.isEmpty()) {
            return Flux.just("抱歉，在知识库中未找到相关信息。");
        }
        
        // 2. 构建上下文
        String context = relevantDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));
        
        // 3. 构建 Prompt
        String userPrompt = String.format(USER_PROMPT_TEMPLATE, context, question);
        
        // 4. 流式调用 LLM
        var promptSpec = chatClient.prompt().system(SYSTEM_PROMPT);
        
        // 添加历史消息（多轮对话）
        if (history != null && !history.isEmpty()) {
            promptSpec = promptSpec.messages(history.toArray(new Message[0]));
        }
        
        return promptSpec
            .user(userPrompt)
            .stream()
            .content();
    }
}