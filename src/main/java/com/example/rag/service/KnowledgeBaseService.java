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
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

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
     * 查询所有知识库
     */
    public List<KnowledgeBaseEntity> listAll() {
        return knowledgeBaseRepository.findAll();
    }
    
    /**
     * 根据ID查询知识库
     */
    public KnowledgeBaseEntity findById(Long id) {
        return knowledgeBaseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("知识库不存在: " + id));
    }
    
    /**
     * 上传知识库（幂等：重复文件返回已有记录）
     */
    @Transactional
    public KnowledgeBaseEntity upload(MultipartFile file, String name, String category) {
        // 1. 计算文件哈希
        String fileHash = calculateHash(file);
        
        // 2. 检查是否已存在
        Optional<KnowledgeBaseEntity> existing = knowledgeBaseRepository.findByFileHash(fileHash);
        if (existing.isPresent()) {
            log.info("文件已存在，返回已有记录: {}", existing.get().getName());
            return existing.get();
        }
        
        // 3. 解析文件
        String content = fileParseService.parseFile(file);
        
        // 4. 保存到数据库
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
        
        // 5. 异步向量化（这里简化为同步）
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
     * 重新处理知识库（向量化）
     * 注意：需要重新上传文件内容，这里只是重置状态
     */
    @Transactional
    public KnowledgeBaseEntity reprocess(Long id) {
        KnowledgeBaseEntity entity = findById(id);
        
        // 重置状态为PENDING
        entity.setVectorStatus(VectorStatus.PENDING);
        entity.setVectorError(null);
        entity.setChunkCount(0);
        
        return knowledgeBaseRepository.save(entity);
    }
    
    /**
     * 重新处理知识库（带文件内容）
     */
    @Transactional
    public KnowledgeBaseEntity reprocessWithContent(Long id, String content) {
        KnowledgeBaseEntity entity = findById(id);
        
        // 更新状态为PROCESSING
        entity.setVectorStatus(VectorStatus.PROCESSING);
        entity.setVectorError(null);
        entity = knowledgeBaseRepository.save(entity);
        
        try {
            // 删除旧向量数据
            vectorService.deleteByKnowledgeBaseId(id);
            
            // 重新向量化
            vectorService.vectorizeAndStore(id, content);
            entity.setVectorStatus(VectorStatus.COMPLETED);
            entity.setChunkCount(1); // 简化处理
        } catch (Exception e) {
            log.error("重新处理失败: {}", id, e);
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