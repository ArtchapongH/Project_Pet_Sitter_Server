package com.techup.pet_sitter.dto;

import java.util.UUID;

public record ConversationPeer(UUID userId, String name, String avatarUrl, String role) {
}
