package com.ev.erp.repository;

import com.ev.erp.model.OtherIncome;
import com.ev.erp.repository.ExcelScootyRepository.ExcelAccessException;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores other incomes in their own local .xlsx file.
 * NOTE: close the file in Excel before using the API, otherwise Windows locks it.
 */
@Repository
public class ExcelOtherIncomeRepository implements OtherIncomeRepository {

    private static final String[] HEADER = {"ID", "Name", "Amount", "Date"};

    private final Path file;
    private final String sheetName;
    private final DataFormatter formatter = new DataFormatter();

    public ExcelOtherIncomeRepository(@Value("${excel.income-file-path}") String filePath,
                                      @Value("${excel.income-sheet}") String sheetName) {
        this.file = Path.of(filePath).toAbsolutePath();
        this.sheetName = sheetName;
    }

    @PostConstruct
    void init() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Workbook wb = Files.exists(file) ? open() : new XSSFWorkbook();
            try (wb) {
                getOrCreateSheet(wb);
                save(wb);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialise Excel file: " + file, e);
        }
    }

    @Override
    public List<OtherIncome> findAll() {
        return read(sheet -> {
            List<OtherIncome> list = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && !text(row, 0).isBlank()) list.add(fromRow(row));
            }
            return list;
        });
    }

    @Override
    public Optional<OtherIncome> findById(String id) {
        return findAll().stream().filter(i -> i.id().equals(id)).findFirst();
    }

    @Override
    public OtherIncome save(OtherIncome income) {
        return write(sheet -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            toRow(income, row);
            return income;
        });
    }

    @Override
    public Optional<OtherIncome> update(OtherIncome income) {
        return write(sheet -> {
            int idx = findRowIndex(sheet, income.id());
            if (idx < 0) return Optional.<OtherIncome>empty();
            toRow(income, sheet.getRow(idx));
            return Optional.of(income);
        });
    }

    @Override
    public boolean deleteById(String id) {
        return write(sheet -> {
            int idx = findRowIndex(sheet, id);
            if (idx < 0) return false;
            int last = sheet.getLastRowNum();
            sheet.removeRow(sheet.getRow(idx));
            if (idx < last) sheet.shiftRows(idx + 1, last, -1);
            return true;
        });
    }

    // ---------- workbook helpers ----------

    private synchronized <T> T read(Function<Sheet, T> action) {
        try (Workbook wb = open()) {
            return action.apply(getOrCreateSheet(wb));
        } catch (IOException e) {
            throw new ExcelAccessException("Failed to read Excel file: " + file, e);
        }
    }

    private synchronized <T> T write(Function<Sheet, T> action) {
        try (Workbook wb = open()) {
            T result = action.apply(getOrCreateSheet(wb));
            save(wb);
            return result;
        } catch (IOException e) {
            throw new ExcelAccessException("Failed to write Excel file (is it open in Excel?): " + file, e);
        }
    }

    private Workbook open() throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return new XSSFWorkbook(in);
        }
    }

    private void save(Workbook wb) throws IOException {
        Path tmp = Files.createTempFile(file.getParent(), "income", ".tmp");
        try (OutputStream out = Files.newOutputStream(tmp)) {
            wb.write(out);
        }
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private Sheet getOrCreateSheet(Workbook wb) {
        Sheet sheet = wb.getSheet(sheetName);
        if (sheet == null) {
            sheet = wb.createSheet(sheetName);
            Row header = sheet.createRow(0);
            CellStyle bold = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            bold.setFont(font);
            for (int i = 0; i < HEADER.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(HEADER[i]);
                c.setCellStyle(bold);
                sheet.setColumnWidth(i, 20 * 256);
            }
        }
        return sheet;
    }

    // ---------- row mapping ----------

    private int findRowIndex(Sheet sheet, String id) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && id.equals(text(row, 0))) return i;
        }
        return -1;
    }

    private void toRow(OtherIncome income, Row row) {
        setText(row, 0, income.id());
        setText(row, 1, income.name());
        row.createCell(2).setCellValue(income.amount().doubleValue()); // numeric so Excel can sum it
        setText(row, 3, income.date().toString());
    }

    private OtherIncome fromRow(Row row) {
        String amount = text(row, 2);
        String date = text(row, 3);
        return new OtherIncome(
                text(row, 0),
                text(row, 1),
                amount.isBlank() ? BigDecimal.ZERO : new BigDecimal(amount),
                date.isBlank() ? null : LocalDate.parse(date));
    }

    private void setText(Row row, int col, String value) {
        row.createCell(col, CellType.STRING).setCellValue(value);
    }

    private String text(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
