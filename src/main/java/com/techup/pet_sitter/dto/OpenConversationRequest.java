package com.techup.pet_sitter.dto;

import java.util.UUID;

public record OpenConversationRequest(UUID sitterId, UUID ownerId) {
}
