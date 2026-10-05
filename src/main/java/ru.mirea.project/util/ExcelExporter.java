package ru.mirea.project.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.model.WasteRequest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Экспортер реестра заявок в формат Microsoft Excel (.xlsx) с использованием Apache POI.
 * Включает встроенную защиту от блокировок файлов на уровне ОС Windows/macOS.
 */
public class ExcelExporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Экспорт списка заявок в файл Excel.
     *
     * @param requests       Список заявок для выгрузки
     * @param targetFilePath Относительный или абсолютный путь к файлу (по умолчанию ./reports/waste_requests.xlsx)
     * @return Абсолютный путь к созданному файлу
     */
    public String exportRequests(List<WasteRequest> requests, String targetFilePath) {
        Path path = Paths.get(targetFilePath);
        try {
            // Гарантированное создание директории, если ее нет
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            throw new BusinessException("Не удалось создать директорию для отчета: " + e.getMessage(), e);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Реестр заявок");

            // Настройка стилей заголовков
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Настройка границ и стилей строк данных
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0.00"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Стили для цветной подсветки статусов
            CellStyle completedStyle = workbook.createCellStyle();
            completedStyle.cloneStyleFrom(dataStyle);
            Font completedFont = workbook.createFont();
            completedFont.setBold(true);
            completedFont.setColor(IndexedColors.GREEN.getIndex());
            completedStyle.setFont(completedFont);

            CellStyle inProgressStyle = workbook.createCellStyle();
            inProgressStyle.cloneStyleFrom(dataStyle);
            Font inProgressFont = workbook.createFont();
            inProgressFont.setBold(true);
            inProgressFont.setColor(IndexedColors.BLUE.getIndex());
            inProgressStyle.setFont(inProgressFont);

            CellStyle cancelledStyle = workbook.createCellStyle();
            cancelledStyle.cloneStyleFrom(dataStyle);
            Font cancelledFont = workbook.createFont();
            cancelledFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            cancelledStyle.setFont(cancelledFont);

            // Создание строки заголовков
            String[] headers = {
                    "№ Заявки", "Клиент", "Адрес вывоза", "Тип отходов", 
                    "Объем (м³)", "Статус", "Дата создания"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Заполнение данных
            int rowIdx = 1;
            for (WasteRequest req : requests) {
                Row row = sheet.createRow(rowIdx++);
                
                Cell c0 = row.createCell(0);
                c0.setCellValue(req.getId());
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                String clientDesc = req.getClientName() != null ? req.getClientName() : "ID: " + req.getClientId();
                c1.setCellValue(clientDesc);
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(req.getAddress());
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(req.getWasteType().getTitle());
                c3.setCellStyle(dataStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(req.getVolumeM3());
                c4.setCellStyle(numberStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(req.getStatus().getTitle());
                CellStyle statusStyle = switch (req.getStatus()) {
                    case COMPLETED -> completedStyle;
                    case IN_PROGRESS -> inProgressStyle;
                    case CANCELLED -> cancelledStyle;
                    default -> dataStyle;
                };
                c5.setCellStyle(statusStyle);

                Cell c6 = row.createCell(6);
                c6.setCellValue(req.getCreatedAt() != null ? req.getCreatedAt().format(DATE_FORMATTER) : "");
                c6.setCellStyle(dataStyle);
            }

            // Автоподбор ширины столбцов под содержимое
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Запись в файл с перехватом системной блокировки Excel
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                workbook.write(fos);
            }

            return path.toAbsolutePath().toString();

        } catch (IOException e) {
            handleIoException(e, path);
            return null;
        }
    }

    /**
     * Экспорт базы клиентов в файл Excel.
     */
    public String exportClients(List<ru.mirea.project.model.Client> clients, String targetFilePath) {
        Path path = Paths.get(targetFilePath);
        ensureDirectoryExists(path);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Клиенты");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"ID", "Наименование контрагента", "Тип клиента", "Телефон", "Email"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ru.mirea.project.model.Client c : clients) {
                Row row = sheet.createRow(rowIdx++);
                Cell c0 = row.createCell(0); c0.setCellValue(c.getId()); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(c.getName()); c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(c.getClientType() != null ? c.getClientType().getTitle() : ""); c2.setCellStyle(dataStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(c.getPhone()); c3.setCellStyle(dataStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(c.getEmail()); c4.setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                workbook.write(fos);
            }
            return path.toAbsolutePath().toString();
        } catch (IOException e) {
            handleIoException(e, path);
            return null;
        }
    }

    /**
     * Экспорт автопарка спецтехники в файл Excel.
     */
    public String exportVehicles(List<ru.mirea.project.model.Vehicle> vehicles, String targetFilePath) {
        Path path = Paths.get(targetFilePath);
        ensureDirectoryExists(path);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Автопарк спецтехники");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"ID", "Госномер", "Модель спецтранспорта", "Вместимость кузова (м³)", "Текущий статус"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ru.mirea.project.model.Vehicle v : vehicles) {
                Row row = sheet.createRow(rowIdx++);
                Cell c0 = row.createCell(0); c0.setCellValue(v.getId()); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(v.getLicensePlate()); c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(v.getModelName()); c2.setCellStyle(dataStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(v.getCapacityM3()); c3.setCellStyle(dataStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(v.getStatus().getTitle()); c4.setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                workbook.write(fos);
            }
            return path.toAbsolutePath().toString();
        } catch (IOException e) {
            handleIoException(e, path);
            return null;
        }
    }

    private void ensureDirectoryExists(Path path) {
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new BusinessException("Не удалось создать директорию: " + e.getMessage(), e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return headerStyle;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        return dataStyle;
    }

    private void handleIoException(IOException e, Path path) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("отказано в доступе") || msg.contains("access is denied") || msg.contains("being used by another process")) {
            throw new BusinessException(
                    "Файл отчета заблокирован другой программой (вероятно, он открыт в Microsoft Excel)!\n" +
                    "Пожалуйста, закройте окно Excel с файлом '" + path.getFileName() + "' и повторите выгрузку.");
        }
        throw new BusinessException("Ошибка ввода/вывода при сохранении Excel-файла: " + e.getMessage(), e);
    }
}
