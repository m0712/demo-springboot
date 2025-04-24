package com.example.excel2db.exception;

import lombok.Getter;

@Getter
public class ExcelImportException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public ExcelImportException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ExcelImportException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public enum ErrorCode {
        INVALID_FILE_FORMAT,
        FILE_TOO_LARGE,
        EMPTY_FILE,
        DATABASE_ERROR,
        DATA_VALIDATION_FAILED,
        UNSUPPORTED_DATABASE,
        TABLE_CREATION_FAILED
    }
}