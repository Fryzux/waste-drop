package ru.mirea.wastemanagement.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mirea.wastemanagement.model.RequestStatus;
import ru.mirea.wastemanagement.model.WasteRequest;
import ru.mirea.wastemanagement.model.WasteType;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExporterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Экспорт в Excel формирует корректные цветные стили для статусов")
    void testExportRequestsWithColorStyling() throws Exception {
        ExcelExporter exporter = new ExcelExporter();

        WasteRequest r1 = new WasteRequest(1L, 1L, "Адрес 1", WasteType.MUNICIPAL, 10.0, RequestStatus.COMPLETED, LocalDateTime.now());
        WasteRequest r2 = new WasteRequest(2L, 1L, "Адрес 2", WasteType.CONSTRUCTION, 20.0, RequestStatus.IN_PROGRESS, LocalDateTime.now());
        WasteRequest r3 = new WasteRequest(3L, 1L, "Адрес 3", WasteType.BULKY, 15.0, RequestStatus.CANCELLED, LocalDateTime.now());
        WasteRequest r4 = new WasteRequest(4L, 1L, "Адрес 4", WasteType.HAZARDOUS, 5.0, RequestStatus.NEW, LocalDateTime.now());

        Path target = tempDir.resolve("export_test.xlsx");
        String path = exporter.exportRequests(List.of(r1, r2, r3, r4), target.toString());

        assertNotNull(path);
        assertTrue(target.toFile().exists());

        try (FileInputStream fis = new FileInputStream(target.toFile());
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            assertNotNull(sheet);
            assertEquals(5, sheet.getPhysicalNumberOfRows()); // 1 header + 4 data rows

            // Проверяем цвет шрифта для COMPLETED (row 1, col 5)
            Row row1 = sheet.getRow(1);
            Cell c1 = row1.getCell(5);
            Font f1 = wb.getFontAt(c1.getCellStyle().getFontIndex());
            assertEquals(IndexedColors.GREEN.getIndex(), f1.getColor());
            assertTrue(f1.getBold());

            // Проверяем цвет шрифта для IN_PROGRESS (row 2, col 5)
            Row row2 = sheet.getRow(2);
            Cell c2 = row2.getCell(5);
            Font f2 = wb.getFontAt(c2.getCellStyle().getFontIndex());
            assertEquals(IndexedColors.BLUE.getIndex(), f2.getColor());
            assertTrue(f2.getBold());

            // Проверяем цвет шрифта для CANCELLED (row 3, col 5)
            Row row3 = sheet.getRow(3);
            Cell c3 = row3.getCell(5);
            Font f3 = wb.getFontAt(c3.getCellStyle().getFontIndex());
            assertEquals(IndexedColors.GREY_50_PERCENT.getIndex(), f3.getColor());
        }
    }
}
