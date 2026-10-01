package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.Booking;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.BookingPetRepository;
import com.techup.pet_sitter.repository.BookingRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SitterBookingServiceTest {
    @Test
    void openingBookingPersistsViewedTimestamp() {
        UUID sitterId = UUID.randomUUID();
        BookingRepository bookings = mock(BookingRepository.class);
        BookingPetRepository bookingPets = mock(BookingPetRepository.class);
        Booking booking = mock(Booking.class);
        User owner = mock(User.class);
        AtomicReference<OffsetDateTime> viewedAt = new AtomicReference<>();
        Clock clock = Clock.fixed(Instant.parse("2026-09-24T08:00:00Z"), ZoneId.of("Asia/Bangkok"));

        when(bookings.findByIdAndSitter_UserId(42L, sitterId)).thenReturn(Optional.of(booking));
        when(booking.getId()).thenReturn(42L);
        when(booking.getStatus()).thenReturn("waiting_confirm");
        when(booking.getOwner()).thenReturn(owner);
        when(booking.getSitterViewedAt()).thenAnswer(ignored -> viewedAt.get());
        doAnswer(invocation -> {
            viewedAt.set(invocation.getArgument(0));
            return null;
        }).when(booking).setSitterViewedAt(org.mockito.ArgumentMatchers.any());
        when(bookingPets.findByBooking_Id(42L)).thenReturn(List.of());

        var response = new SitterBookingService(bookings, bookingPets, clock).get(sitterId, 42L);

        assertNotNull(response.sitterViewedAt());
        assertEquals(Instant.parse("2026-09-24T08:00:00Z"), response.sitterViewedAt().toInstant());
        verify(bookings).save(booking);
    }
}
