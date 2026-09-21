package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.SitterBookingResponse;
import com.techup.pet_sitter.entity.Booking;
import com.techup.pet_sitter.entity.BookingPet;
import com.techup.pet_sitter.repository.BookingPetRepository;
import com.techup.pet_sitter.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SitterBookingService {
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private final BookingRepository bookings;
    private final BookingPetRepository bookingPets;
    private final Clock clock;

    public SitterBookingService(BookingRepository bookings, BookingPetRepository bookingPets) {
        this(bookings, bookingPets, Clock.system(BANGKOK));
    }

    SitterBookingService(BookingRepository bookings, BookingPetRepository bookingPets, Clock clock) {
        this.bookings = bookings;
        this.bookingPets = bookingPets;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<SitterBookingResponse> list(UUID sitterId, String query, LocalDate from, LocalDate to) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return bookings.findBySitter_UserIdOrderByCreatedAtDesc(sitterId).stream()
                .filter(item -> from == null || !item.getEndDate().isBefore(from))
                .filter(item -> to == null || !item.getStartDate().isAfter(to))
                .map(this::toResponse)
                .filter(item -> needle.isEmpty() || matches(item, needle))
                .toList();
    }

    public List<SitterBookingResponse> list(UUID sitterId, String query) {
        return list(sitterId, query, null, null);
    }

    @Transactional(readOnly = true)
    public SitterBookingResponse get(UUID sitterId, Long id) {
        return toResponse(requireOwned(sitterId, id));
    }

    @Transactional
    public SitterBookingResponse changeStatus(UUID sitterId, Long id, String next) {
        Booking booking = requireOwned(sitterId, id);
        if ("waiting_service".equals(next) && "waiting_confirm".equals(booking.getStatus())) {
            booking.setStatus(next);
        } else if ("cancelled".equals(next) && "waiting_confirm".equals(booking.getStatus())) {
            booking.setStatus(next);
        } else if ("success".equals(next) && "waiting_service".equals(booking.getStatus()) && hasEnded(booking)) {
            booking.setStatus(next);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid booking status transition");
        }
        return toResponse(bookings.save(booking));
    }

    private Booking requireOwned(UUID sitterId, Long id) {
        return bookings.findByIdAndSitter_UserId(id, sitterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private boolean hasEnded(Booking booking) {
        return !LocalDateTime.now(clock).isBefore(LocalDateTime.of(booking.getEndDate(), booking.getEndTime()));
    }

    private String displayStatus(Booking booking) {
        if ("waiting_service".equals(booking.getStatus())
                && !LocalDateTime.now(clock).isBefore(LocalDateTime.of(booking.getStartDate(), booking.getStartTime()))) {
            return "in_service";
        }
        return booking.getStatus();
    }

    private boolean matches(SitterBookingResponse item, String needle) {
        return item.owner().name().toLowerCase(Locale.ROOT).contains(needle)
                || (item.transactionNo() != null && item.transactionNo().toLowerCase(Locale.ROOT).contains(needle))
                || item.pets().stream().anyMatch(p -> p.name().toLowerCase(Locale.ROOT).contains(needle));
    }

    private SitterBookingResponse toResponse(Booking booking) {
        var owner = booking.getOwner();
        List<SitterBookingResponse.Pet> pets = bookingPets.findByBooking_Id(booking.getId()).stream()
                .map(BookingPet::getPet)
                .map(p -> new SitterBookingResponse.Pet(p.getId(), p.getName(), p.getBreed(), p.getSex(),
                        p.getAgeMonths(), p.getColor(), p.getWeightKg(), p.getAbout(), p.getAvatarUrl()))
                .toList();
        return new SitterBookingResponse(booking.getId(), displayStatus(booking), booking.getStartDate(),
                booking.getEndDate(), booking.getStartTime(), booking.getEndTime(), booking.getDuration(),
                booking.getDurationUnit(), booking.getTotalPrice(), booking.getTransactionNo(), booking.getCreatedAt(),
                booking.getAdditionalMessage(), new SitterBookingResponse.Owner(owner.getName(), owner.getEmail(),
                owner.getPhone(), owner.getAvatarUrl()), pets);
    }
}
