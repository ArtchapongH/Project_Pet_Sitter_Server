package com.techup.pet_sitter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PetRequest(
        @NotBlank String name,
        @NotBlank String petType,
        @NotBlank String breed,
        @NotBlank @Pattern(regexp = "Male|Female") String sex,
        @NotNull @Min(0) Integer ageMonths,
        @NotBlank String color,
        @NotNull @Min(0) Float weightKg,
        String about,
        String avatarUrl
) {
}
