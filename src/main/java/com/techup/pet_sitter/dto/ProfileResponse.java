package com.techup.pet_sitter.dto;

import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String approvalStatus,
        boolean listed,
        String rejectionReason,
        ProfilePayload profile,
        ProfilePayload pendingProfile
) {
}
