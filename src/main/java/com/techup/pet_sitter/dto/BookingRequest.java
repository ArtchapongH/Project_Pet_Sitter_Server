package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
        String paymentMethod,
        List<Long> petIds,
        String cardOwnerName,
        String cardNumber
) {
    public BookingRequest(
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
        this(sitterId, startDate, endDate, startTime, endTime, duration, durationUnit,
                contactName, contactEmail, contactPhone, additionalMessage, totalPrice,
                paymentMethod, null, null, null);
    }
}
