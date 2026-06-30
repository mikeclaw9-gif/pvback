package com.pventabase.reportes.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pventabase.reportes.dto.ReporteData;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class PdfGenerator {

    public byte[] generarPdf(ReporteData data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLUE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

            Paragraph title = new Paragraph(data.getTitulo(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            if (data.getColumnas() != null && !data.getColumnas().isEmpty()) {
                PdfPTable table = new PdfPTable(data.getColumnas().size());
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);
                table.setSpacingAfter(10);

                for (String colName : data.getColumnas()) {
                    PdfPCell header = new PdfPCell(new Phrase(colName, headerFont));
                    header.setBackgroundColor(new Color(41, 128, 185));
                    header.setPadding(5);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                }

                if (data.getFilas() != null) {
                    for (Map<String, Object> fila : data.getFilas()) {
                        for (String colName : data.getColumnas()) {
                            Object val = fila.get(colName);
                            PdfPCell cell = new PdfPCell(new Phrase(formatValue(val), cellFont));
                            cell.setPadding(3);
                            table.addCell(cell);
                        }
                    }
                }

                document.add(table);
            } else if (data.getFilas() != null) {
                for (Map<String, Object> fila : data.getFilas()) {
                    for (Map.Entry<String, Object> entry : fila.entrySet()) {
                        document.add(new Paragraph(entry.getKey() + ": " + formatValue(entry.getValue()), cellFont));
                    }
                    document.add(new Paragraph(" "));
                }
            }

            if (data.getGraficoNombre() != null && data.getTipoGrafico() != com.pventabase.reportes.enums.TipoGrafico.NONE) {
                document.add(new Paragraph(" "));
                Paragraph chartTitle = new Paragraph(data.getGraficoNombre(), titleFont);
                chartTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(chartTitle);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof BigDecimal) return "$" + String.format("%,.2f", value);
        if (value instanceof Number) return String.format("%,.2f", value);
        if (value instanceof LocalDateTime || value instanceof LocalDate) return value.toString();
        if (value instanceof Boolean) return (Boolean) value ? "Si" : "No";
        return value.toString();
    }
}
