package com.ev.erp.repository;

import com.ev.erp.model.InventoryItem;
import com.ev.erp.model.ItemCategory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores inventory in its own local .xlsx file.
 * NOTE: close the file in Excel before using the API, otherwise Windows locks it.
 */
@Repository
public class ExcelInventoryRepository implements InventoryRepository {

    private static final String[] HEADER = {"ID", "Name", "Category", "Quantity", "Unit Price", "Description"};

    private final Path file;
    private final String sheetName;
    private final DataFormatter formatter = new DataFormatter();

    public ExcelInventoryRepository(@Value("${excel.inventory-file-path}") String filePath,
                                    @Value("${excel.inventory-sheet}") String sheetName) {
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
    public List<InventoryItem> findAll() {
        return read(sheet -> {
            List<InventoryItem> list = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && !text(row, 0).isBlank()) list.add(fromRow(row));
            }
            return list;
        });
    }

    @Override
    public Optional<InventoryItem> findById(String id) {
        return findAll().stream().filter(i -> i.id().equals(id)).findFirst();
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return write(sheet -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            toRow(item, row);
            return item;
        });
    }

    @Override
    public Optional<InventoryItem> update(InventoryItem item) {
        return write(sheet -> {
            int idx = findRowIndex(sheet, item.id());
            if (idx < 0) return Optional.<InventoryItem>empty();
            toRow(item, sheet.getRow(idx));
            return Optional.of(item);
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
        Path tmp = Files.createTempFile(file.getParent(), "inventory", ".tmp");
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

    private void toRow(InventoryItem item, Row row) {
        setText(row, 0, item.id());
        setText(row, 1, item.name());
        setText(row, 2, item.category().name());
        row.createCell(3).setCellValue(item.quantity());              // numeric
        if (item.unitPrice() == null) {
            setText(row, 4, "");
        } else {
            row.createCell(4).setCellValue(item.unitPrice().doubleValue());
        }
        setText(row, 5, item.description() == null ? "" : item.description());
    }

    private InventoryItem fromRow(Row row) {
        String qty = text(row, 3);
        String price = text(row, 4);
        return new InventoryItem(
                text(row, 0),
                text(row, 1),
                ItemCategory.valueOf(text(row, 2)),
                qty.isBlank() ? 0 : new BigDecimal(qty).intValue(),
                price.isBlank() ? null : new BigDecimal(price),
                text(row, 5));
    }

    private void setText(Row row, int col, String value) {
        row.createCell(col, CellType.STRING).setCellValue(value);
    }

    private String text(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}