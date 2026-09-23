package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class OwnerProfileRules {
    private OwnerProfileRules() {
    }

    public static boolean isComplete(User user) {
        return hasText(user.getName())
                && hasText(user.getPhone())
                && hasText(user.getIdNumber())
                && user.getDateOfBirth() != null;
    }

    public static void requireForBooking(User user) {
        if (!isComplete(user)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Complete your name, phone, ID number and date of birth before booking"
            );
        }
    }

    public static void requireNotBanned(User user) {
        if (Boolean.TRUE.equals(user.isBanned())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is banned");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
