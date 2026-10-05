package com.techup.pet_sitter.dto;

import java.util.UUID;

public record ConversationResponse(
        Long id,
        UUID participantId,
        String participantName,
        String participantAvatar,
        String lastMessage,
        long unreadCount
) {
}
