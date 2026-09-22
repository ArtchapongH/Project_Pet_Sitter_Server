package com.techup.pet_sitter.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record OwnerProfileUpdateRequest(
        @NotBlank String name,
        @NotBlank String phone,
        String idNumber,
        LocalDate dateOfBirth,
        String avatarUrl
) {
}
