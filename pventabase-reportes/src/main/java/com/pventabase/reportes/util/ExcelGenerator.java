package com.pventabase.reportes.util;

import com.pventabase.reportes.dto.ReporteData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ExcelGenerator {

    public byte[] generarExcel(ReporteData data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(data.getTitulo());

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue(data.getTitulo());
            titleRow.getCell(0).setCellStyle(titleStyle);
            if (data.getColumnas() != null && !data.getColumnas().isEmpty()) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, data.getColumnas().size() - 1));
            }
            rowNum++;

            if (data.getColumnas() != null && !data.getColumnas().isEmpty()) {
                Row headerRow = sheet.createRow(rowNum++);
                for (int i = 0; i < data.getColumnas().size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(data.getColumnas().get(i));
                    cell.setCellStyle(headerStyle);
                    sheet.autoSizeColumn(i);
                }
            }

            if (data.getFilas() != null) {
                for (Map<String, Object> fila : data.getFilas()) {
                    Row row = sheet.createRow(rowNum++);
                    int col = 0;
                    if (data.getColumnas() != null) {
                        for (String colName : data.getColumnas()) {
                            Object val = fila.get(colName);
                            Cell cell = row.createCell(col);
                            setCellValue(cell, val);
                            cell.setCellStyle(cellStyle);
                            col++;
                        }
                    }
                }
            }

            if (data.getGraficoNombre() != null && data.getTipoGrafico() != com.pventabase.reportes.enums.TipoGrafico.NONE) {
                rowNum += 2;
                Row chartTitleRow = sheet.createRow(rowNum++);
                chartTitleRow.createCell(0).setCellValue(data.getGraficoNombre());
                chartTitleRow.getCell(0).setCellStyle(titleStyle);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel", e);
        }
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(value.toString());
        } else if (value instanceof LocalDate) {
            cell.setCellValue(value.toString());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
