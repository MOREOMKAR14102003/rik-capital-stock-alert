package in.rikcapital.stockalert.service;

import in.rikcapital.stockalert.model.Company;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class CompanyExcelService {
    private final List<Company> companies = new CopyOnWriteArrayList<>();

    @Value("${stock-alert.excel-file:classpath:data/companies.xlsx}")
    private String excelFile;

    @Value("${stock-alert.sheet-name:}")
    private String sheetName;

    @Value("${stock-alert.header-row:1}")
    private int configuredHeaderRow;

    @PostConstruct
    public void loadCompanies() {
        try (InputStream in = openExcel()) {
            Workbook workbook = WorkbookFactory.create(in);
            Sheet sheet = selectSheet(workbook);
            if (sheet.getPhysicalNumberOfRows() == 0) return;

            int headerRowIndex = Math.max(0, configuredHeaderRow - 1);
            Row header = sheet.getRow(headerRowIndex);
            if (header == null) throw new IllegalStateException("Configured Excel header row not found: " + configuredHeaderRow);

            Map<String, Integer> columns = headerMap(header);
            // The supplied RIK Capital workbook uses a two-row header on Q2_Potential_Client:
            // row 2 contains NSE/BSE/Company Name and row 3 contains Symbol/Scrip Code.
            Row secondHeader = sheet.getRow(headerRowIndex + 1);
            if (secondHeader != null) mergeHeaderMap(columns, secondHeader);

            int nameCol = find(columns, "company name", "name", "company", "name_of_company");
            int codeCol = find(columns, "scrip code", "bse", "code", "company code");
            int symbolCol = find(columns, "symbol", "nse", "ticker", "ticker symbol");

            if (nameCol < 0) {
                throw new IllegalStateException("Excel must contain a Company Name/Name column.");
            }

            int dataStart = headerRowIndex + (secondHeader == null ? 1 : 2);
            for (int i = dataStart; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String name = cell(row, nameCol);
                if (name.isBlank()) continue;
                companies.add(new Company(name, cell(row, codeCol), cell(row, symbolCol)));
            }
            workbook.close();
            System.out.println("Loaded " + companies.size() + " companies from Excel.");
        } catch (Exception e) {
            System.err.println("Could not load company Excel file: " + e.getMessage());
        }
    }

    public List<Company> search(String query, int limit) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isBlank()) return companies.stream().limit(limit).toList();
        return companies.stream()
                .filter(c -> contains(c.name(), q) || contains(c.code(), q) || contains(c.symbol(), q))
                .limit(limit)
                .toList();
    }

    public boolean exists(String companyValue) {
        if (companyValue == null) return false;
        String v = companyValue.trim();
        return companies.stream().anyMatch(c -> c.name().equalsIgnoreCase(v)
                || (!c.code().isBlank() && c.code().equalsIgnoreCase(v))
                || (!c.symbol().isBlank() && c.symbol().equalsIgnoreCase(v)));
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private InputStream openExcel() throws Exception {
        if (excelFile.startsWith("classpath:")) {
            return new ClassPathResource(excelFile.substring("classpath:".length())).getInputStream();
        }
        return Files.newInputStream(Path.of(excelFile));
    }

    private Map<String, Integer> headerMap(Row row) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell c : row) {
            map.put(normalize(cellValue(c)), c.getColumnIndex());
        }
        return map;
    }

    private void mergeHeaderMap(Map<String, Integer> map, Row row) {
        for (Cell c : row) {
            String value = normalize(cellValue(c));
            if (!value.isBlank()) map.putIfAbsent(value, c.getColumnIndex());
        }
    }

    private String cellValue(Cell c) {
        return new DataFormatter().formatCellValue(c).trim();
    }

    private Sheet selectSheet(Workbook workbook) {
        if (sheetName != null && !sheetName.isBlank()) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new IllegalStateException("Excel sheet not found: " + sheetName);
            return sheet;
        }
        return workbook.getSheetAt(0);
    }

    private int find(Map<String, Integer> map, String... names) {
        for (String n : names) {
            Integer idx = map.get(normalize(n));
            if (idx != null) return idx;
        }
        return -1;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String cell(Row row, int col) {
        if (col < 0) return "";
        Cell c = row.getCell(col);
        if (c == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(c).trim();
    }
}
