package com.techup.pet_sitter.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record OwnerScheduleRequest(
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime
) {
}
