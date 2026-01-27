package com.castellanos94.tracking.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.castellanos94.tracking.db.TimeReportDAO;
import com.castellanos94.tracking.model.FormatExportEnum;
import com.castellanos94.tracking.model.TimeReport;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TimeReportExportService {
    private final TimeReportDAO timeReportDAO;
    private final List<String> header = Arrays.asList("category", "description", "start", "end", "hours", "rate",
            "amount");

    public TimeReportExportService() {
        this.timeReportDAO = new TimeReportDAO();
    }

    public void export(LocalDate start, LocalDate end, FormatExportEnum format, String path)
            throws SQLException, IOException {
        List<TimeReport> timeReports = start == null || end == null ? timeReportDAO.findAll()
                : timeReportDAO.findAll(start.atStartOfDay(), end.atTime(23, 59, 59));
        export(timeReports, format, path);
    }

    public void export(List<TimeReport> timeReports, FormatExportEnum format, String path)
            throws SQLException, IOException {
        if (timeReports == null || timeReports.isEmpty()) {
            throw new IllegalArgumentException("No time reports to export");
        }
        if (Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("File already exists: " + path);
        }
        String pathFile = path;
        if (!path.endsWith("." + format.name().toLowerCase())) {
            pathFile = path + "." + format.name().toLowerCase();
        }
        switch (format) {
            case JSON:
                exportToJSON(timeReports, pathFile);
                break;
            case XLSX:
                exportToXLSX(timeReports, pathFile);
                break;
            case CSV:
                exportToCSV(timeReports, pathFile);
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private void exportToJSON(List<TimeReport> timeReports, String path) throws FileNotFoundException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (FileOutputStream fileOut = new FileOutputStream(path)) {
            mapper.writeValue(fileOut, timeReports);
        }
    }

    private void exportToXLSX(List<TimeReport> timeReports, String path) throws FileNotFoundException, IOException {
        Path file = Paths.get(path);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Time Reports");
            XSSFRow row = sheet.createRow(0);
            for (int i = 0; i < header.size(); i++) {
                row.createCell(i).setCellValue(header.get(i));
            }
            int rowNum = 1;
            for (TimeReport timeReport : timeReports) {
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(timeReport.getCategory());
                row.createCell(1).setCellValue(timeReport.getDescription());
                row.createCell(2).setCellValue(timeReport.getStart());
                row.createCell(3).setCellValue(timeReport.getEnd());
                row.createCell(4).setCellValue(timeReport.getHours());
                row.createCell(5).setCellValue(timeReport.getRate());
                row.createCell(6).setCellValue(timeReport.getAmount());
            }
            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }
        }

    }

    private void exportToCSV(List<TimeReport> timeReports, String path) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append(String.join(",", header));
        content.append("\n");
        for (TimeReport timeReport : timeReports) {
            content.append(timeReport.getCategory()).append(",");
            content.append(timeReport.getDescription()).append(",");
            content.append(timeReport.getStart()).append(",");
            content.append(timeReport.getEnd()).append(",");
            content.append(timeReport.getHours()).append(",");
            content.append(timeReport.getRate()).append(",");
            content.append(timeReport.getAmount()).append(",");
            content.append("\n");
        }
        Files.write(Paths.get(path), content.toString().getBytes(StandardCharsets.UTF_8));
    }
}