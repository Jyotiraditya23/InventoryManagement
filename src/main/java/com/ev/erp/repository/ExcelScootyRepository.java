package com.ev.erp.repository;

import com.ev.erp.model.Scooty;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stores scooties in a local .xlsx file. One row = one scooty.
 * Columns: A id | B ownerName | C date | D amount | E batteryNumbers | F bikeName
 *          G chassisNo | H motorNo | I chargerNo
 *
 * NOTE: close the file in Excel before using the API, otherwise Windows locks it and writes fail.
 */
@Repository
public class ExcelScootyRepository implements ScootyRepository {

    private static final String[] HEADER = {
            "ID", "Owner Name", "Date", "Amount", "Battery Numbers",
            "Bike Name", "Chassis No", "Motor No", "Charger No"};
    private static final String BATTERY_SEPARATOR = ",";

    private final Path file;
    private final String sheetName;
    private final DataFormatter formatter = new DataFormatter();

    public ExcelScootyRepository(@Value("${excel.file-path}") String filePath,
                                 @Value("${excel.scooty-sheet}") String sheetName) {
        this.file = Path.of(filePath).toAbsolutePath();
        this.sheetName = sheetName;
    }

    /** Creates the folder, file, sheet and header row if they don't exist yet. */
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
    public List<Scooty> findAll() {
        return read(sheet -> {
            List<Scooty> list = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && !text(row, 0).isBlank()) list.add(fromRow(row));
            }
            return list;
        });
    }

    @Override
    public Optional<Scooty> findById(String id) {
        return findAll().stream().filter(s -> s.id().equals(id)).findFirst();
    }

    @Override
    public Scooty save(Scooty scooty) {
        return write(sheet -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            toRow(scooty, row);
            return scooty;
        });
    }

    @Override
    public Optional<Scooty> update(Scooty scooty) {
        return write(sheet -> {
            int idx = findRowIndex(sheet, scooty.id());
            if (idx < 0) return Optional.<Scooty>empty();
            toRow(scooty, sheet.getRow(idx));
            return Optional.of(scooty);
        });
    }

    @Override
    public boolean deleteById(String id) {
        return write(sheet -> {
            int idx = findRowIndex(sheet, id);
            if (idx < 0) return false;
            int last = sheet.getLastRowNum();
            sheet.removeRow(sheet.getRow(idx));
            if (idx < last) sheet.shiftRows(idx + 1, last, -1); // close the gap
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

    /** Writes to a temp file first, then swaps it in, so a failure can't corrupt the data file. */
    private void save(Workbook wb) throws IOException {
        Path tmp = Files.createTempFile(file.getParent(), "shop-data", ".tmp");
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

    private void toRow(Scooty s, Row row) {
        setText(row, 0, s.id());
        setText(row, 1, s.ownerName());
        setText(row, 2, s.date().toString());
        row.createCell(3).setCellValue(s.amount().doubleValue()); // numeric so you can sum it in Excel
        setText(row, 4, String.join(BATTERY_SEPARATOR, s.batteryNumbers()));
        setText(row, 5, s.bikeName());
        setText(row, 6, s.chassisNo());
        setText(row, 7, s.motorNo());
        setText(row, 8, s.chargerNo());
    }

    private Scooty fromRow(Row row) {

        String batteries = text(row, 4).trim();

        String date = text(row, 2);
        String amount = text(row, 3);

        return new Scooty(
                text(row, 0),
                text(row, 1),
                date.isBlank() ? null : LocalDate.parse(date),
                amount.isBlank() ? BigDecimal.ZERO : new BigDecimal(amount),
                batteries,
                text(row, 5),
                text(row, 6),
                text(row, 7),
                text(row, 8)
        );
    }

    private void setText(Row row, int col, String value) {
        Cell cell = row.createCell(col, CellType.STRING); // keep chassis/motor numbers as text
        cell.setCellValue(value);
    }

    private String text(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    public static class ExcelAccessException extends RuntimeException {
        public ExcelAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}