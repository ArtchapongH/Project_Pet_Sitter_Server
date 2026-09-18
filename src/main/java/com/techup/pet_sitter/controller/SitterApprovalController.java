package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.ListedSitterSearchResponse;
import com.techup.pet_sitter.dto.ProfilePayload;
import com.techup.pet_sitter.dto.ProfileResponse;
import com.techup.pet_sitter.dto.RejectRequest;
import com.techup.pet_sitter.service.SitterApprovalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SitterApprovalController {
    private final SitterApprovalService approvals;

    public SitterApprovalController(SitterApprovalService approvals) {
        this.approvals = approvals;
    }

    @GetMapping("/sitter/profile")
    ProfileResponse ownProfile(@RequestHeader("X-User-Id") UUID userId) {
        return approvals.getOwnProfile(userId);
    }

    @PostMapping("/sitter/profile/submit")
    ProfileResponse submit(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody ProfilePayload payload
    ) {
        return approvals.submit(userId, payload);
    }

    @GetMapping("/admin/sitter-approvals")
    List<ProfileResponse> queue(@RequestHeader("X-Admin-Id") UUID adminId) {
        return approvals.approvalQueue(adminId);
    }

    @PatchMapping("/admin/sitter-approvals/approve")
    ProfileResponse approve(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam UUID sitterId
    ) {
        return approvals.approve(adminId, sitterId);
    }

    @PatchMapping("/admin/sitter-approvals/reject")
    ProfileResponse reject(
            @RequestHeader("X-Admin-Id") UUID adminId,
            @RequestParam UUID sitterId,
            @RequestBody RejectRequest request
    ) {
        return approvals.reject(adminId, sitterId, request.reason());
    }

    @GetMapping("/sitters")
    ListedSitterSearchResponse listedSitters(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(name = "petType", required = false) List<String> petTypes,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "") String experience,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return approvals.searchListed(keyword, petTypes, minRating, experience, page, limit);
    }
}
