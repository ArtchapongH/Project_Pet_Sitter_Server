package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PublicSitterDetailResponse(
        UUID userId,
        String displayName,
        String avatarUrl,
        String ownerName,
        String introduction,
        String services,
        String myPlace,
        String addressDetail,
        String subDistrict,
        String district,
        String province,
        String postCode,
        String experienceYears,
        List<String> petTypes,
        List<String> photoUrls,
        BigDecimal ratingAvg,
        Integer reviewCount,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
