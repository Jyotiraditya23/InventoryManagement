package com.ev.erp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ScootyResponse(
        String id,
        String ownerName,
        LocalDate date,
        BigDecimal amount,
        String batteryNumbers,
        String bikeName,
        String chassisNo,
        String motorNo,
        String chargerNo
) {}
