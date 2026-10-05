package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        Long id,
        Long conversationId,
        UUID senderId,
        String content,
        String imageUrl,
        OffsetDateTime sentAt,
        boolean mine
) {
}
