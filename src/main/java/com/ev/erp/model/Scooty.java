package com.ev.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * One scooty sale/record. Maps to one row in the "Scooty" sheet tab.
 * Columns: A id | B ownerName | C date | D amount | E batteryNumbers | F bikeName
 *          G chassisNo | H motorNo | I chargerNo
 */
public record Scooty(
        String id,
        String ownerName,
        LocalDate date,
        BigDecimal amount,
        String batteryNumbers,
        String bikeName,
        String chassisNo,
        String motorNo,
        String chargerNo
){}
