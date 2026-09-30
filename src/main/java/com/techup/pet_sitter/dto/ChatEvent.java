package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatEvent(
        String type,
        Long conversationId,
        MessageResponse message,
        UUID readerId,
        OffsetDateTime readAt
) {
}
