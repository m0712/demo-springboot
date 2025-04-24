package com.example.excel2db.service;

import com.example.excel2db.dto.ExcelImportRequest;
import com.example.excel2db.exception.ExcelImportException;
import com.example.excel2db.utils.DataCleaner;
import com.example.excel2db.utils.DataTypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final DataSource dataSource;
    private final DataTypeResolver dataTypeResolver;
    private final DataCleaner dataCleaner;

    @Transactional
    public int importExcel(ExcelImportRequest request) {
        try (Workbook workbook = WorkbookFactory.create(request.getFile().getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (request.isSkipHeader() && rowIterator.hasNext()) {
                rowIterator.next(); // Skip header row
            }

            // Process first data row to determine column types
            Row firstDataRow = rowIterator.hasNext() ? rowIterator.next() : null;
            if (firstDataRow == null) {
                throw new ExcelImportException(ExcelImportException.ErrorCode.EMPTY_FILE, 
                    "Excel file contains no data");
            }

            // Get column names and types
            List<String> columnNames = getColumnNames(sheet.getRow(0));
            Map<String, String> columnTypes = dataTypeResolver.resolveColumnTypes(firstDataRow, columnNames);

            // Create table if needed
            if (request.isAutoCreateTable()) {
                createTable(request.getTableName(), columnNames, columnTypes, request.getPrimaryKey());
            }

            // Import data
            int importedRows = importData(request.getTableName(), columnNames, Arrays.asList(firstDataRow));
            while (rowIterator.hasNext()) {
                importedRows += importData(request.getTableName(), columnNames, Arrays.asList(rowIterator.next()));
            }

            return importedRows;
        } catch (IOException e) {
            throw new ExcelImportException(ExcelImportException.ErrorCode.INVALID_FILE_FORMAT, 
                "Failed to read Excel file", e);
        }
    }

    private List<String> getColumnNames(Row headerRow) {
        List<String> columnNames = new ArrayList<>();
        for (Cell cell : headerRow) {
            columnNames.add(cell.getStringCellValue().trim().replaceAll("\\s+", "_").toLowerCase());
        }
        return columnNames;
    }

    private void createTable(String tableName, List<String> columnNames, 
                           Map<String, String> columnTypes, String primaryKey) {
        String sql = generateCreateTableSql(tableName, columnNames, columnTypes, primaryKey);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            log.debug("Executing SQL: {}", sql);
            statement.execute(sql);
        } catch (SQLException e) {
            throw new ExcelImportException(ExcelImportException.ErrorCode.TABLE_CREATION_FAILED, 
                "Failed to create table: " + tableName, e);
        }
    }

    private String generateCreateTableSql(String tableName, List<String> columnNames, 
                                        Map<String, String> columnTypes, String primaryKey) {
        StringBuilder sql = new StringBuilder("CREATE TABLE ")
                .append(tableName)
                .append(" (");

        for (String columnName : columnNames) {
            sql.append(columnName)
               .append(" ")
               .append(columnTypes.get(columnName))
               .append(", ");
        }

        if (primaryKey != null && columnNames.contains(primaryKey)) {
            sql.append("PRIMARY KEY (").append(primaryKey).append(")");
        } else {
            sql.setLength(sql.length() - 2); // Remove trailing comma
        }

        sql.append(")");
        return sql.toString();
    }

    private int importData(String tableName, List<String> columnNames, List<Row> rows) {
        String sql = generateInsertSql(tableName, columnNames);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            
            for (Row row : rows) {
                for (int i = 0; i < columnNames.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Object value = getCellValue(cell);
                    value = dataCleaner.cleanData(columnNames.get(i), value);
                    ps.setObject(i + 1, value);
                }
                ps.addBatch();
            }
            
            int[] results = ps.executeBatch();
            return Arrays.stream(results).sum();
        } catch (SQLException e) {
            throw new ExcelImportException(ExcelImportException.ErrorCode.DATABASE_ERROR, 
                "Failed to import data to table: " + tableName, e);
        }
    }

    private String generateInsertSql(String tableName, List<String> columnNames) {
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append(" (")
                .append(String.join(", ", columnNames))
                .append(") VALUES (")
                .append(String.join(", ", Collections.nCopies(columnNames.size(), "?")))
                .append(")");
        return sql.toString();
    }

    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                // 修复公式单元格处理
                return getCellValue(cell.getSheet().getWorkbook().getCreationHelper()
                        .createFormulaEvaluator().evaluateInCell(cell));
            default:
                return null;
        }
    }

}