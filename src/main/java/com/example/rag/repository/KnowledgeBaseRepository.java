package com.example.rag.repository;

import com.example.rag.entity.KnowledgeBaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 知识库Repository
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseEntity, Long> {
    
    /**
     * 根据文件哈希查询
     */
    Optional<KnowledgeBaseEntity> findByFileHash(String fileHash);
}