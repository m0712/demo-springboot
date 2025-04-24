package com.example.excel2db.controller;

import com.example.excel2db.dto.ExcelImportRequest;
import com.example.excel2db.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelImportController {

    private final ExcelImportService excelImportService;

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(@Valid ExcelImportRequest request) {
        log.info("Received request to import excel file: {}", request.getFile().getOriginalFilename());
        int importedRows = excelImportService.importExcel(request);
        return ResponseEntity.ok("Successfully imported " + importedRows + " rows");
    }
}