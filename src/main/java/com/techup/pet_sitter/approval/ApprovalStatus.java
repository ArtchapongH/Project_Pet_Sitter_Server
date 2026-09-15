package com.techup.pet_sitter.approval;

import java.util.Arrays;

public enum ApprovalStatus {
    UNVERIFIED("Unverified"),
    WAITING_FOR_VERIFY("Waiting for verify"),
    VERIFIED("Verified"),
    WAITING_FOR_APPROVE("Waiting for approve"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String value;

    ApprovalStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ApprovalStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown approval status: " + value));
    }

    public ApprovalStatus afterSubmit() {
        return switch (this) {
            case UNVERIFIED -> WAITING_FOR_VERIFY;
            case VERIFIED, APPROVED, REJECTED -> WAITING_FOR_APPROVE;
            default -> throw new IllegalStateException("Profile is already waiting for Admin approval");
        };
    }

    public ApprovalStatus afterApprove() {
        return switch (this) {
            case WAITING_FOR_VERIFY -> VERIFIED;
            case WAITING_FOR_APPROVE -> APPROVED;
            default -> throw new IllegalStateException("Profile is not waiting for approval");
        };
    }

    public ApprovalStatus afterReject() {
        return switch (this) {
            case WAITING_FOR_VERIFY -> UNVERIFIED;
            case WAITING_FOR_APPROVE -> REJECTED;
            default -> throw new IllegalStateException("Profile is not waiting for approval");
        };
    }
}
