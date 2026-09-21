package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingAdminListItem(
        Long id,
        String ownerName,
        Long petCount,
        BigDecimal duration,
        String durationUnit,
        LocalDate startDate,
        LocalTime startTime,
        String status
) {
}
