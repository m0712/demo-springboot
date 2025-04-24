package com.example.excel2db.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ExcelImportRequest {
    @NotNull(message = "Excel file is required")
    private MultipartFile file;
    
    @NotBlank(message = "Table name is required")
    private String tableName;
    
    private boolean autoCreateTable = true;
    private boolean skipHeader = true;
    private String primaryKey;
}