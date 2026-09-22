package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;

public record ReviewAdminListItem(
        Long id,
        String ownerName,
        String ownerAvatarUrl,
        OffsetDateTime createdAt,
        Short rating,
        String comment
) {
}
