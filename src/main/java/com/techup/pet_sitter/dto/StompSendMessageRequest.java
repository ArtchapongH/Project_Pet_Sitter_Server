package com.techup.pet_sitter.dto;

public record StompSendMessageRequest(Long conversationId, String content, String imageUrl) {
}
