package com.example.rag.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 向量数据Repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class VectorRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 检查vector_store表是否存在
     */
    public boolean tableExists() {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'vector_store')";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
    
    /**
     * 根据知识库ID删除向量数据
     */
    public int deleteByKnowledgeBaseId(Long knowledgeBaseId) {
        if (!tableExists()) {
            log.info("vector_store表不存在，跳过删除");
            return 0;
        }
        String sql = "DELETE FROM vector_store WHERE metadata->>'kb_id' = ?";
        return jdbcTemplate.update(sql, knowledgeBaseId.toString());
    }
}