package com.example.rag.dto;

import lombok.Data;
import java.util.List;

/**
 * RAG查询请求
 */
@Data
public class QueryRequest {
    private List<Long> knowledgeBaseIds;
    private String question;
}