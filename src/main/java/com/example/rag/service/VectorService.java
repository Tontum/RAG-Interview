package com.example.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化服务
 */
@Slf4j
@Service
public class VectorService {
    
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    
    public VectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        // 默认配置：每个 chunk 约 800 tokens
        this.textSplitter = TokenTextSplitter.builder()
            .withChunkSize(800)
            .build();
    }
    
    /**
     * 将文本向量化并存储
     * 
     * @param knowledgeBaseId 知识库ID
     * @param content 文本内容
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void vectorizeAndStore(Long knowledgeBaseId, String content) {
        log.info("开始向量化知识库: kbId={}, contentLength={}", knowledgeBaseId, content.length());
        
        // 1. 先删除该知识库的旧向量数据
        deleteByKnowledgeBaseId(knowledgeBaseId);
        
        // 2. 将文本分块
        List<Document> chunks = textSplitter.apply(List.of(new Document(content)));
        log.info("文本分块完成: {} 个chunks", chunks.size());
        
        // 3. 为每个 chunk 添加 metadata（知识库ID）
        List<Document> documentsWithMetadata = chunks.stream()
            .map(chunk -> {
                Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                metadata.put("kb_id", knowledgeBaseId.toString());
                return new Document(chunk.getText(), metadata);
            })
            .collect(Collectors.toList());
        
        // 4. 分批向量化并存储
        int batchSize = 10;
        for (int i = 0; i < documentsWithMetadata.size(); i += batchSize) {
            List<Document> batch = documentsWithMetadata.subList(i, Math.min(i + batchSize, documentsWithMetadata.size()));
            vectorStore.add(batch);
        }
        
        log.info("知识库向量化完成: kbId={}, chunks={}", knowledgeBaseId, documentsWithMetadata.size());
    }
    
    /**
     * 相似度搜索
     * 
     * @param query 查询文本
     * @param knowledgeBaseIds 知识库ID列表
     * @param topK 返回结果数量
     * @param minScore 最小相似度
     * @return 相关文档列表
     */
    public List<Document> similaritySearch(String query, List<Long> knowledgeBaseIds, 
                                           int topK, double minScore) {
        SearchRequest.Builder builder = SearchRequest.builder()
            .query(query)
            .topK(topK);
        
        if (minScore > 0) {
            builder.similarityThreshold(minScore);
        }
        
        // 添加知识库过滤条件
        if (knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty()) {
            String filterExpression = knowledgeBaseIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(", "));
            builder.filterExpression("kb_id in [" + filterExpression + "]");
        }
        
        return vectorStore.similaritySearch(builder.build());
    }
    
    /**
     * 删除指定知识库的向量数据
     */
    public void deleteByKnowledgeBaseId(Long knowledgeBaseId) {
        // 这里调用 VectorRepository 或直接使用 JdbcTemplate
        // 详见 VectorRepository.java
    }
}