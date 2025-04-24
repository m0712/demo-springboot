package com.example.excel2db.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 扩展版ErrorResponse示例
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String errorCode;
    private String message;
    private long timestamp;
    private String path;  // 请求路径
    private List<ValidationError> errors; // 用于字段验证错误
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
    
    // 便捷构造方法
    public ErrorResponse(String errorCode, String message) {
        this(errorCode, message, System.currentTimeMillis(), null, null);
    }
    
    // 用于验证错误的构造方法
    public ErrorResponse(List<ValidationError> errors) {
        this("VALIDATION_ERROR", "Validation failed", System.currentTimeMillis(), null, errors);
    }
}