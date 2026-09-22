package com.techup.pet_sitter.dto;

public record BookingPetInfo(
        Long id,
        String name,
        String type,
        String avatarUrl
) {
}
