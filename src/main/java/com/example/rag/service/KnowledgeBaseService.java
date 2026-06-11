package com.example.rag.service;

import com.example.rag.entity.KnowledgeBaseEntity;
import com.example.rag.entity.VectorStatus;
import com.example.rag.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 知识库管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {
    
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final FileParseService fileParseService;
    private final VectorService vectorService;
    
    /**
     * 上传知识库
     */
    @Transactional
    public KnowledgeBaseEntity upload(MultipartFile file, String name, String category) {
        // 1. 解析文件
        String content = fileParseService.parseFile(file);
        
        // 2. 计算文件哈希
        String fileHash = calculateHash(file);
        
        // 3. 保存到数据库
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setUserId(1L); // TODO: 从 Session 获取
        entity.setFileHash(fileHash);
        entity.setName(name != null ? name : file.getOriginalFilename());
        entity.setCategory(category);
        entity.setOriginalFilename(file.getOriginalFilename());
        entity.setFileSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setVectorStatus(VectorStatus.PENDING);
        
        entity = knowledgeBaseRepository.save(entity);
        
        // 4. 异步向量化（这里简化为同步）
        try {
            vectorService.vectorizeAndStore(entity.getId(), content);
            entity.setVectorStatus(VectorStatus.COMPLETED);
        } catch (Exception e) {
            entity.setVectorStatus(VectorStatus.FAILED);
            entity.setVectorError(e.getMessage());
        }
        
        return knowledgeBaseRepository.save(entity);
    }
    
    /**
     * 计算文件哈希
     */
    private String calculateHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("计算文件哈希失败", e);
        }
    }
}