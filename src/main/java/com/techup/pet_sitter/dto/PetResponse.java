package com.techup.pet_sitter.dto;

public record PetResponse(
        Long id,
        String name,
        String petType,
        String breed,
        String sex,
        Integer ageMonths,
        String color,
        Float weightKg,
        String about,
        String avatarUrl
) {
}
