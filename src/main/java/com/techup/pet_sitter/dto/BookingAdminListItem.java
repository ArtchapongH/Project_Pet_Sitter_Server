package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BookingAdminListItem(
        Long id,
        String ownerName,
        BigDecimal totalPrice,
        BigDecimal duration,
        String durationUnit,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        String status,
        List<BookingPetInfo> pets
) {
}
