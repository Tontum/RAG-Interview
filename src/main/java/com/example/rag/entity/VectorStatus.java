package com.example.rag.entity;

/**
 * 向量化状态枚举
 */
public enum VectorStatus {
    PENDING,    // 待处理
    PROCESSING, // 处理中
    COMPLETED,  // 已完成
    FAILED      // 失败
}