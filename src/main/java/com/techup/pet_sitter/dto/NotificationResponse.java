package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;

public record NotificationResponse(Long id, String type, String content, boolean read, OffsetDateTime createdAt) {
}
