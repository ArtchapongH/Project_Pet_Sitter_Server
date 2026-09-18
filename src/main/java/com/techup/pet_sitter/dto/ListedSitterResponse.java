package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ListedSitterResponse(
        UUID userId,
        String displayName,
        String avatarUrl,
        List<String> petTypes,
        String services,
        String introduction,
        String province,
        String ownerName,
        String imageUrl,
        String experienceYears,
        BigDecimal ratingAvg,
        Integer reviewCount
) {
}
