package com.example.rag.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库实体类
 */
@Entity
@Table(name = "knowledge_bases")
@Data
public class KnowledgeBaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 64)
    private String fileHash;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 100)
    private String category;
    
    @Column(nullable = false)
    private String originalFilename;
    
    private Long fileSize;
    
    private String contentType;
    
    @Column(length = 500)
    private String storageKey;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "access_count")
    private Integer accessCount = 0;
    
    @Column(name = "question_count")
    private Integer questionCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VectorStatus vectorStatus = VectorStatus.PENDING;
    
    @Column(length = 500)
    private String vectorError;
    
    @Column(name = "chunk_count")
    private Integer chunkCount = 0;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}