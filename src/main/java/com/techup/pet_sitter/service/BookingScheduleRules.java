package com.techup.pet_sitter.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class BookingScheduleRules {
    private BookingScheduleRules() {
    }

    public static void requireChronology(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, Clock clock) {
        if (startDate == null || endDate == null || startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking dates and times are required");
        }
        LocalDateTime start = LocalDateTime.of(startDate, startTime);
        LocalDateTime end = LocalDateTime.of(endDate, endTime);
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after the start time");
        }
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected time must be in the future");
        }
    }

    public static boolean overlaps(LocalDateTime start, LocalDateTime end, LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && end.isAfter(otherStart);
    }

    public static boolean datesOverlap(LocalDate start, LocalDate end, LocalDate otherStart, LocalDate otherEnd) {
        return !end.isBefore(otherStart) && !start.isAfter(otherEnd);
    }

    public static boolean sameDayOfWeek(LocalDate date, short stored) {
        int value = stored == 0 ? 7 : stored;
        return value == date.getDayOfWeek().getValue();
    }

    public static boolean slotCovers(LocalTime slotStart, LocalTime slotEnd, LocalTime start, LocalTime end) {
        return !slotEnd.isBefore(slotStart) && !start.isBefore(slotStart) && !end.isAfter(slotEnd);
    }

    public static String ownerStatus(String stored, LocalDate startDate, LocalTime startTime, Clock clock) {
        if ("success".equals(stored)) return "completed";
        if ("cancelled".equals(stored)) return "cancelled";
        if ("waiting_service".equals(stored) || "in_service".equals(stored)) {
            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            return LocalDateTime.now(clock).isBefore(start) ? "confirmed" : "in_service";
        }
        return "pending";
    }
}
