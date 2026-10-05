package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.Booking;
import com.techup.pet_sitter.entity.SitterAvailability;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.SitterUnavailability;
import com.techup.pet_sitter.repository.BookingRepository;
import com.techup.pet_sitter.repository.SitterAvailabilityRepository;
import com.techup.pet_sitter.repository.SitterUnavailabilityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class BookingScheduleGuard {
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private final BookingRepository bookings;
    private final SitterAvailabilityRepository availability;
    private final SitterUnavailabilityRepository unavailability;
    private final Clock clock;

    @Autowired
    public BookingScheduleGuard(BookingRepository bookings, SitterAvailabilityRepository availability,
                                SitterUnavailabilityRepository unavailability) {
        this(bookings, availability, unavailability, Clock.system(BANGKOK));
    }

    BookingScheduleGuard(BookingRepository bookings, SitterAvailabilityRepository availability,
                         SitterUnavailabilityRepository unavailability, Clock clock) {
        this.bookings = bookings;
        this.availability = availability;
        this.unavailability = unavailability;
        this.clock = clock;
    }

    public void requireAvailable(SitterProfile sitter, LocalDate startDate, LocalDate endDate,
                                 LocalTime startTime, LocalTime endTime, Long ignoreBookingId) {
        BookingScheduleRules.requireChronology(startDate, endDate, startTime, endTime, clock);
        LocalDateTime start = LocalDateTime.of(startDate, startTime);
        LocalDateTime end = LocalDateTime.of(endDate, endTime);
        requireNoBlockedDates(sitter, startDate, endDate);
        requireWeeklyAvailability(sitter, startDate, endDate, startTime, endTime);
        requireNoOverlap(sitter, start, end, ignoreBookingId);
    }

    private void requireNoBlockedDates(SitterProfile sitter, LocalDate startDate, LocalDate endDate) {
        for (SitterUnavailability block : unavailability.findBySitter_UserId(sitter.getUserId())) {
            if (BookingScheduleRules.datesOverlap(startDate, endDate, block.getStartDate(), block.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Pet sitter is not available for the selected dates");
            }
        }
    }

    private void requireWeeklyAvailability(SitterProfile sitter, LocalDate startDate, LocalDate endDate,
                                           LocalTime startTime, LocalTime endTime) {
        List<SitterAvailability> slots = availability.findBySitter_UserId(sitter.getUserId()).stream()
                .filter(SitterAvailability::isAvailable)
                .toList();
        if (slots.isEmpty()) return;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate current = date;
            boolean covered = slots.stream()
                    .filter(slot -> BookingScheduleRules.sameDayOfWeek(current, slot.getDayOfWeek()))
                    .anyMatch(slot -> coversDay(slot, current, startDate, endDate, startTime, endTime));
            if (!covered) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Pet sitter is not available for the selected time");
            }
        }
    }

    private boolean coversDay(SitterAvailability slot, LocalDate date, LocalDate startDate, LocalDate endDate,
                              LocalTime startTime, LocalTime endTime) {
        if (startDate.equals(endDate)) {
            return BookingScheduleRules.slotCovers(slot.getStartTime(), slot.getEndTime(), startTime, endTime);
        }
        if (date.equals(startDate)) return !startTime.isBefore(slot.getStartTime());
        if (date.equals(endDate)) return !endTime.isAfter(slot.getEndTime());
        return true;
    }

    private void requireNoOverlap(SitterProfile sitter, LocalDateTime start, LocalDateTime end, Long ignoreBookingId) {
        for (Booking existing : bookings.findBySitter_UserIdOrderByCreatedAtDesc(sitter.getUserId())) {
            if ("cancelled".equals(existing.getStatus()) || existing.getId().equals(ignoreBookingId)) continue;
            LocalDateTime otherStart = LocalDateTime.of(existing.getStartDate(), existing.getStartTime());
            LocalDateTime otherEnd = LocalDateTime.of(existing.getEndDate(), existing.getEndTime());
            if (BookingScheduleRules.overlaps(start, end, otherStart, otherEnd)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Pet sitter already has a booking at this time");
            }
        }
    }
}
