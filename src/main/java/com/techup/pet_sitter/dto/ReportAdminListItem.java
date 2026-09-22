package com.techup.pet_sitter.dto;

import java.time.OffsetDateTime;

public record ReportAdminListItem(
        Long id,
        String userName,
        String reportedPersonName,
        String issue,
        String description,
        OffsetDateTime createdAt,
        String status
) {
}
