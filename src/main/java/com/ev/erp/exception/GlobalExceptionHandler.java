package com.ev.erp.exception;

import com.ev.erp.repository.ExcelScootyRepository.ExcelAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One place that turns every exception into a clean JSON error:
 * { "timestamp": "...", "status": 404, "message": "..." }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 404 : record not found ----------
    @ExceptionHandler({ScootyNotFoundException.class, ExpenseNotFoundException.class,
            InventoryItemNotFoundException.class, OtherIncomeNotFoundException.class})
    public ResponseEntity<Map<String, Object>> notFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ---------- 409 : insufficient stock / duplicate inventory item ----------
    @ExceptionHandler({InsufficientStockException.class, DuplicateInventoryItemException.class})
    public ResponseEntity<Map<String, Object>> conflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ---------- 400 : @Valid failures (missing/blank/negative fields) ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", fields);
        return ResponseEntity.badRequest().body(body);
    }

    // ---------- 400 : malformed JSON, bad date in body, unknown category in body ----------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid request body. Check JSON format, dates (yyyy-MM-dd) and category (SCOOTY, BATTERY or OTHERS).");
    }

    // ---------- 400 : bad query parameter (?month=2026-13, ?category=BIKE, ?from=abc) ----------
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> badParam(MethodArgumentTypeMismatchException ex) {
        Class<?> type = ex.getRequiredType();
        String expected;
        if (type != null && type.isEnum()) {
            expected = Arrays.toString(type.getEnumConstants());
        } else if (type == YearMonth.class) {
            expected = "yyyy-MM (e.g. 2026-11)";
        } else if (type == LocalDate.class) {
            expected = "yyyy-MM-dd (e.g. 2026-11-07)";
        } else {
            expected = type == null ? "a valid value" : type.getSimpleName();
        }
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'. Expected: " + expected);
    }

    // ---------- 500 : Excel file problems (locked, unreadable, ...) ----------
    @ExceptionHandler(ExcelAccessException.class)
    public ResponseEntity<Map<String, Object>> excel(ExcelAccessException ex) {
        log.error("Excel access error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // ---------- fallback ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> fallback(Exception ex) {
        // Spring's own web exceptions (404 unknown URL, 405 wrong method, 415 ...) keep their status
        if (ex instanceof ErrorResponse er) {
            HttpStatusCode status = er.getStatusCode();
            return ResponseEntity.status(status)
                    .body(base(status, er.getBody().getDetail() != null ? er.getBody().getDetail() : "Request failed"));
        }
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }

    // ---------- helpers ----------
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(base(status, message));
    }

    private Map<String, Object> base(HttpStatusCode status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        return body;
    }
}