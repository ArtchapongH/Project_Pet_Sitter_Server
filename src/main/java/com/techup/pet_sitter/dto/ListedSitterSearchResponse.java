package com.techup.pet_sitter.dto;

import java.util.List;

public record ListedSitterSearchResponse(
        List<ListedSitterResponse> sitters,
        int currentPage,
        int totalPages,
        long totalItems,
        int limit
) {
}
