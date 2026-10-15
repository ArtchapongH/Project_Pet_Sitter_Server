package com.techup.pet_sitter.dto;

import java.util.List;

public record MessagePageResponse(List<MessageResponse> items, boolean hasMore) {
}
