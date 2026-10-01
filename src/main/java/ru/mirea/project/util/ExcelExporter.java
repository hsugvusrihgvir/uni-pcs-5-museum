package ru.mirea.project.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.project.model.Booking;
import ru.mirea.project.model.Exhibition;
import ru.mirea.project.model.Visitor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ExcelExporter {

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ExcelExporter() {
    }

    public static Path exportAll(
            List<Visitor> visitors,
            List<Exhibition> exhibitions,
            List<Booking> bookings
    ) {
        Path exportDirectory = Path.of("exports");
        String fileName = "museum_export_"
                + LocalDateTime.now().format(FILE_DATE_FORMAT)
                + ".xlsx";
        Path exportFile = exportDirectory.resolve(fileName);

        try {
            Files.createDirectories(exportDirectory);

            try (Workbook workbook = new XSSFWorkbook();
                 OutputStream outputStream = Files.newOutputStream(exportFile)) {
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle dateStyle = createDateStyle(workbook);

                createVisitorsSheet(workbook, visitors, headerStyle, dateStyle);
                createExhibitionsSheet(workbook, exhibitions, headerStyle, dateStyle);
                createBookingsSheet(workbook, bookings, headerStyle, dateStyle);

                workbook.write(outputStream);
            }

            return exportFile.toAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось экспортировать данные в Excel.", e);
        }
    }

    private static void createVisitorsSheet(
            Workbook workbook,
            List<Visitor> visitors,
            CellStyle headerStyle,
            CellStyle dateStyle
    ) {
        Sheet sheet = workbook.createSheet("Посетители");
        String[] headers = {
                "ID", "Имя", "Фамилия", "Отчество", "Дата рождения", "Email"
        };
        createHeader(sheet, headers, headerStyle);

        int rowNumber = 1;
        for (Visitor visitor : visitors) {
            Row row = sheet.createRow(rowNumber++);
            row.createCell(0).setCellValue(visitor.getId());
            setStringCell(row, 1, visitor.getName());
            setStringCell(row, 2, visitor.getLastName());
            setStringCell(row, 3, visitor.getPatronymic());
            setDateCell(row, 4, visitor.getBirthDate(), dateStyle);
            setStringCell(row, 5, visitor.getEmail());
        }

        autoSizeColumns(sheet, headers.length);
    }

    private static void createExhibitionsSheet(
            Workbook workbook,
            List<Exhibition> exhibitions,
            CellStyle headerStyle,
            CellStyle dateStyle
    ) {
        Sheet sheet = workbook.createSheet("Выставки");
        String[] headers = {
                "ID", "Название", "Описание", "URL фотографии",
                "Дата начала", "Дата окончания", "Номер зала"
        };
        createHeader(sheet, headers, headerStyle);

        int rowNumber = 1;
        for (Exhibition exhibition : exhibitions) {
            Row row = sheet.createRow(rowNumber++);
            row.createCell(0).setCellValue(exhibition.getId());
            setStringCell(row, 1, exhibition.getTitle());
            setStringCell(row, 2, exhibition.getDescription());
            setStringCell(row, 3, exhibition.getPhotoUrl());
            setDateCell(row, 4, exhibition.getStartDate(), dateStyle);
            setDateCell(row, 5, exhibition.getEndDate(), dateStyle);
            row.createCell(6).setCellValue(exhibition.getHallNumber());
        }

        autoSizeColumns(sheet, headers.length);
    }

    private static void createBookingsSheet(
            Workbook workbook,
            List<Booking> bookings,
            CellStyle headerStyle,
            CellStyle dateStyle
    ) {
        Sheet sheet = workbook.createSheet("Бронирования");
        String[] headers = {
                "ID", "ID посетителя", "ID выставки", "Дата посещения", "Статус", "Цена"
        };
        createHeader(sheet, headers, headerStyle);

        int rowNumber = 1;
        for (Booking booking : bookings) {
            Row row = sheet.createRow(rowNumber++);
            row.createCell(0).setCellValue(booking.getId());
            row.createCell(1).setCellValue(booking.getVisitorId());
            row.createCell(2).setCellValue(booking.getExhibitionId());
            setDateCell(row, 3, booking.getVisitDate(), dateStyle);
            setStringCell(
                    row,
                    4,
                    booking.getStatus() == null ? null : booking.getStatus().name()
            );
            if (booking.getPrice() != null) {
                row.createCell(5).setCellValue(booking.getPrice());
            }
        }

        autoSizeColumns(sheet, headers.length);
    }

    private static void createHeader(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 1);
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(
                workbook.getCreationHelper().createDataFormat().getFormat("dd.mm.yyyy")
        );
        return style;
    }

    private static void setStringCell(Row row, int column, String value) {
        row.createCell(column).setCellValue(value == null ? "" : value);
    }

    private static void setDateCell(
            Row row,
            int column,
            LocalDate value,
            CellStyle dateStyle
    ) {
        if (value == null) {
            return;
        }

        Cell cell = row.createCell(column);
        cell.setCellValue(java.sql.Date.valueOf(value));
        cell.setCellStyle(dateStyle);
    }

    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
