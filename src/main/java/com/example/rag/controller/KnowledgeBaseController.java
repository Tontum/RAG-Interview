package com.example.rag.controller;

import com.example.rag.entity.KnowledgeBaseEntity;
import com.example.rag.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库管理Controller
 */
@RestController
@RequestMapping("/api/knowledgebase")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    
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
}