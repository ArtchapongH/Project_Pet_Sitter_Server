package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;

public record PublicReviewResponse(
        Long id,
        String ownerName,
        String ownerAvatarUrl,
        Short rating,
        String comment,
        OffsetDateTime createdAt
) {
}
