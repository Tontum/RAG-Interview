package com.example.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件解析服务
 */
@Slf4j
@Service
public class FileParseService {
    
    private final Tika tika = new Tika();
    
    /**
     * 解析文件，提取文本内容
     */
    public String parseFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String content = tika.parseToString(inputStream);
            log.info("文件解析完成: {}, 文本长度: {}", file.getOriginalFilename(), content.length());
            return content;
        } catch (IOException e) {
            log.error("文件解析失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 检测文件类型
     */
    public String detectContentType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            return file.getContentType();
        }
    }
}