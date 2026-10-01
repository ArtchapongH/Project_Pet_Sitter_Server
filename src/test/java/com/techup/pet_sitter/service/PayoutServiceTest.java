package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.repository.BookingRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayoutServiceTest {
    @Test
    void readsPayoutWithoutLockingTheProfile() {
        SitterProfileRepository profiles = mock(SitterProfileRepository.class);
        BookingRepository bookings = mock(BookingRepository.class);
        PayoutService service = new PayoutService(bookings, profiles);
        UUID sitterId = UUID.randomUUID();
        SitterProfile profile = new SitterProfile();
        profile.setUserId(sitterId);
        when(profiles.findById(sitterId)).thenReturn(Optional.of(profile));
        when(bookings.findBySitter_UserIdOrderByCreatedAtDesc(sitterId)).thenReturn(List.of());

        var result = service.get(sitterId);

        assertEquals(0, result.totalEarning().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.transactions().size());
        verify(profiles, never()).findForUpdate(sitterId);
    }
}
