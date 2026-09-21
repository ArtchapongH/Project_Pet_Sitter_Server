package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerProfileRulesTest {
    @Test
    void incompleteUntilIdAndBirthdayAreSet() {
        User user = new User();
        user.setName("Ada");
        user.setPhone("0812345678");
        assertFalse(OwnerProfileRules.isComplete(user));
        user.setIdNumber("1234567890123");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        assertTrue(OwnerProfileRules.isComplete(user));
    }

    @Test
    void bookingRequiresCompleteProfile() {
        assertThrows(ResponseStatusException.class, () -> OwnerProfileRules.requireForBooking(new User()));
    }

    @Test
    void bannedUsersAreRejected() {
        User user = new User();
        user.setBanned(true);
        assertThrows(ResponseStatusException.class, () -> OwnerProfileRules.requireNotBanned(user));
    }
}
