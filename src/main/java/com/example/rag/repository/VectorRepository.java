package com.example.rag.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 向量数据Repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class VectorRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 根据知识库ID删除向量数据
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteByKnowledgeBaseId(Long knowledgeBaseId) {
        String sql = "DELETE FROM vector_store WHERE metadata->>'kb_id' = ?";
        return jdbcTemplate.update(sql, knowledgeBaseId.toString());
    }
}