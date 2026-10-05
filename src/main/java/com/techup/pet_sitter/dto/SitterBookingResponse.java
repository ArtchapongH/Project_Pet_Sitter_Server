package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public record SitterBookingResponse(
        Long id, String status, LocalDate startDate, LocalDate endDate,
        LocalTime startTime, LocalTime endTime, BigDecimal duration, String durationUnit,
        BigDecimal totalPrice, String transactionNo, OffsetDateTime transactionDate,
        OffsetDateTime sitterViewedAt, String additionalMessage, Owner owner, List<Pet> pets
) {
    public record Owner(String name, String email, String phone, String avatarUrl) {}
    public record Pet(Long id, String name, String breed, String sex, Integer ageMonths,
                      String color, Float weightKg, String about, String avatarUrl) {}
}
