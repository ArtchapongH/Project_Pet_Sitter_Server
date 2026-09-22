package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OwnerReviewItem(
        Long id,
        UUID sitterId,
        String sitterName,
        Short rating,
        String comment,
        OffsetDateTime createdAt
) {
}
