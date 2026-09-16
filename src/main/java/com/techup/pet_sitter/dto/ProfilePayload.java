package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfilePayload(
        String fullName,
        String phone,
        String email,
        String experienceYears,
        LocalDate dateOfBirth,
        String idNumber,
        String avatarUrl,
        String introduction,
        String displayName,
        List<String> petTypes,
        String services,
        String myPlace,
        List<String> photoUrls,
        String addressDetail,
        String district,
        String subDistrict,
        String province,
        String postCode,
        BigDecimal latitude,
        BigDecimal longitude,
        String bankName,
        String accountName,
        String accountNumber,
        String bankCode,
        String bookBankImageUrl
) {
    public ProfilePayload {
        petTypes = petTypes == null ? List.of() : List.copyOf(petTypes);
        photoUrls = photoUrls == null ? List.of() : List.copyOf(photoUrls);
    }
}
