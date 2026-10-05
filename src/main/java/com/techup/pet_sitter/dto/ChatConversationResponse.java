package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatConversationResponse(
        Long id,
        UUID ownerId,
        UUID sitterId,
        ConversationPeer peer,
        MessageResponse lastMessage,
        long unreadCount,
        OffsetDateTime createdAt
) {
}
