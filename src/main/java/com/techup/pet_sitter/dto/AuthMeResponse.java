package com.techup.pet_sitter.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AuthMeResponse(
        UUID id,
        String role,
        String name,
        String email,
        String phone,
        String idNumber,
        LocalDate dateOfBirth,
        String avatarUrl,
        boolean profileComplete,
        boolean banned
) {
}
