package com.example.excel2db.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataTypeResolver {

    public Map<String, String> resolveColumnTypes(Row row, List<String> columnNames) {
        Map<String, String> columnTypes = new HashMap<>();
        
        for (int i = 0; i < columnNames.size(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            columnTypes.put(columnNames.get(i), determineDataType(cell));
        }
        
        return columnTypes;
    }

    private String determineDataType(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return "VARCHAR(255)";
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return "TIMESTAMP";
                }
                return "DECIMAL(19,4)";
            case BOOLEAN:
                return "BOOLEAN";
            default:
                return "VARCHAR(255)";
        }
    }
}