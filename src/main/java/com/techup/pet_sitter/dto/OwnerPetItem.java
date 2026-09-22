package com.techup.pet_sitter.dto;

public record OwnerPetItem(
        Long id,
        String name,
        String breed,
        String sex,
        Integer ageMonths,
        String avatarUrl,
        String petTypeName,
        Boolean isSuspended
) {
}
