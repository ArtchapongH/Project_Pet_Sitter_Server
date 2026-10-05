package com.techup.pet_sitter.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingScheduleRulesTest {
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private final Clock clock = Clock.fixed(
            LocalDateTime.of(2026, 10, 5, 9, 0).atZone(BANGKOK).toInstant(),
            BANGKOK
    );

    @Test
    void rejectsScheduleThatDoesNotMoveForward() {
        assertThrows(ResponseStatusException.class, () -> BookingScheduleRules.requireChronology(
                LocalDate.of(2026, 10, 6),
                LocalDate.of(2026, 10, 6),
                LocalTime.of(10, 0),
                LocalTime.of(10, 0),
                clock
        ));
    }

    @Test
    void detectsOverlappingIntervalsAndDates() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 6, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 6, 12, 0);
        assertTrue(BookingScheduleRules.overlaps(start, end, start.plusHours(1), end.plusHours(1)));
        assertFalse(BookingScheduleRules.overlaps(start, end, end, end.plusHours(1)));
        assertTrue(BookingScheduleRules.datesOverlap(
                LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 8),
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 9)
        ));
    }

    @Test
    void mapsStoredStatusForTheOwner() {
        assertEquals("pending", BookingScheduleRules.ownerStatus("waiting_confirm", LocalDate.of(2026, 10, 6), LocalTime.of(7, 0), clock));
        assertEquals("confirmed", BookingScheduleRules.ownerStatus("waiting_service", LocalDate.of(2026, 10, 6), LocalTime.of(10, 0), clock));
        assertEquals("in_service", BookingScheduleRules.ownerStatus("waiting_service", LocalDate.of(2026, 10, 5), LocalTime.of(8, 0), clock));
        assertEquals("completed", BookingScheduleRules.ownerStatus("success", LocalDate.of(2026, 10, 1), LocalTime.of(8, 0), clock));
    }

    @Test
    void matchesBothDayOfWeekConventions() {
        LocalDate monday = LocalDate.of(2026, 10, 5);
        assertTrue(BookingScheduleRules.sameDayOfWeek(monday, (short) 1));
        assertTrue(BookingScheduleRules.sameDayOfWeek(LocalDate.of(2026, 10, 4), (short) 0));
        assertTrue(BookingScheduleRules.slotCovers(LocalTime.of(7, 0), LocalTime.of(18, 0), LocalTime.of(9, 0), LocalTime.of(12, 0)));
    }
}
