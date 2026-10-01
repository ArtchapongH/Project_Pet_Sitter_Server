package com.techup.pet_sitter.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OwnerAdminDetail(
        UUID id,
        String name,
        String email,
        String phone,
        String idNumber,
        LocalDate dateOfBirth,
        String avatarUrl,
        Boolean isBanned,
        List<OwnerPetItem> pets,
        List<OwnerReviewItem> reviews
) {
}
