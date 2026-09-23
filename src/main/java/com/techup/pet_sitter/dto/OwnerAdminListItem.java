package com.techup.pet_sitter.dto;

import java.util.UUID;

public record OwnerAdminListItem(
        UUID id,
        String name,
        String phone,
        String email,
        Long petCount,
        Boolean isBanned,
        String avatarUrl
) {
}
