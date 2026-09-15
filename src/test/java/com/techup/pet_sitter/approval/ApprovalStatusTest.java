package com.techup.pet_sitter.approval;

import com.techup.pet_sitter.approval.ApprovalStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApprovalStatusTest {
    @Test
    void exposesExactlyTheSixErdStatuses() {
        assertEquals(
                List.of("Unverified", "Waiting for verify", "Verified", "Waiting for approve", "Approved", "Rejected"),
                List.of(ApprovalStatus.values()).stream().map(ApprovalStatus::value).toList()
        );
    }

    @Test
    void followsInitialApprovalAndRejectionFlow() {
        assertEquals(ApprovalStatus.WAITING_FOR_VERIFY, ApprovalStatus.UNVERIFIED.afterSubmit());
        assertEquals(ApprovalStatus.VERIFIED, ApprovalStatus.WAITING_FOR_VERIFY.afterApprove());
        assertEquals(ApprovalStatus.UNVERIFIED, ApprovalStatus.WAITING_FOR_VERIFY.afterReject());
        assertEquals(ApprovalStatus.WAITING_FOR_APPROVE, ApprovalStatus.VERIFIED.afterSubmit());
        assertEquals(ApprovalStatus.APPROVED, ApprovalStatus.WAITING_FOR_APPROVE.afterApprove());
        assertEquals(ApprovalStatus.REJECTED, ApprovalStatus.WAITING_FOR_APPROVE.afterReject());
    }

    @Test
    void approvedEditsReturnToTheSecondApprovalQueue() {
        assertEquals(ApprovalStatus.WAITING_FOR_APPROVE, ApprovalStatus.APPROVED.afterSubmit());
        assertEquals(ApprovalStatus.WAITING_FOR_APPROVE, ApprovalStatus.REJECTED.afterSubmit());
        assertThrows(IllegalStateException.class, ApprovalStatus.WAITING_FOR_APPROVE::afterSubmit);
    }
}
