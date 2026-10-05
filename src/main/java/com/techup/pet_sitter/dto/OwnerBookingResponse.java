package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OwnerBookingResponse(
        Long id,
        String sitterId,
        String sitterName,
        String sitterOwner,
        String sitterAvatar,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal durationHours,
        String durationLabel,
        List<String> petNames,
        BigDecimal totalPrice,
        String transactionNo,
        String transactionDate,
        String bannerText,
        String completedAt,
        String mapQuery,
        Review review
) {
    public record Review(int rating, String comment, String createdAt) {
    }
}
