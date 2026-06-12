package com.example.rag.controller;

import com.example.rag.entity.KnowledgeBaseEntity;
import com.example.rag.service.FileParseService;
import com.example.rag.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理Controller
 */
@RestController
@RequestMapping("/api/knowledgebase")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    private final FileParseService fileParseService;
    
    /**
     * 查询知识库列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<KnowledgeBaseEntity>> list() {
        return ResponseEntity.ok(knowledgeBaseService.listAll());
    }
    
    /**
     * 上传知识库
     */
    @PostMapping("/upload")
    public ResponseEntity<KnowledgeBaseEntity> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category) {
        
        KnowledgeBaseEntity result = knowledgeBaseService.upload(file, name, category);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 重新处理知识库（需要重新上传文件）
     */
    @PostMapping("/{id}/reprocess")
    public ResponseEntity<?> reprocess(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // 解析文件内容
            String content = fileParseService.parseFile(file);
            // 重新处理
            KnowledgeBaseEntity result = knowledgeBaseService.reprocessWithContent(id, content);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}