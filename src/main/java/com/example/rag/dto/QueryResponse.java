package com.example.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * RAG查询响应
 */
@Data
@AllArgsConstructor
public class QueryResponse {
    private String answer;
}