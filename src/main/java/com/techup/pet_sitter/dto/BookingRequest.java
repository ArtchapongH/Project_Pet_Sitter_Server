package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingRequest(
        UUID sitterId,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal duration,
        String durationUnit,
        String contactName,
        String contactEmail,
        String contactPhone,
        String additionalMessage,
        BigDecimal totalPrice,
        String paymentMethod
) {
}
