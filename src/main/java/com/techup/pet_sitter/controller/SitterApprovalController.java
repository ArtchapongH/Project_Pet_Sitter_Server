package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.ListedSitterResponse;
import com.techup.pet_sitter.dto.ListedSitterSearchResponse;
import com.techup.pet_sitter.dto.ProfilePayload;
import com.techup.pet_sitter.dto.ProfileResponse;
import com.techup.pet_sitter.dto.PublicReviewResponse;
import com.techup.pet_sitter.dto.PublicSitterDetailResponse;
import com.techup.pet_sitter.dto.RejectRequest;
import com.techup.pet_sitter.security.JwtUser;
import com.techup.pet_sitter.service.SitterApprovalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    ProfileResponse ownProfile(@AuthenticationPrincipal Jwt jwt) {
        return approvals.getOwnProfile(JwtUser.id(jwt));
    }

    @PostMapping("/sitter/profile/submit")
    ProfileResponse submit(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProfilePayload payload
    ) {
        return approvals.submit(JwtUser.id(jwt), payload);
    }

    @GetMapping("/admin/sitter-approvals")
    List<ProfileResponse> queue(@AuthenticationPrincipal Jwt jwt) {
        return approvals.approvalQueue(JwtUser.id(jwt));
    }

    @GetMapping("/admin/sitter-approvals/{sitterId}")
    ProfileResponse adminProfile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sitterId
    ) {
        return approvals.adminProfile(JwtUser.id(jwt), sitterId);
    }

    @PatchMapping("/admin/sitter-approvals/approve")
    ProfileResponse approve(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID sitterId
    ) {
        return approvals.approve(JwtUser.id(jwt), sitterId);
    }

    @PatchMapping("/admin/sitter-approvals/reject")
    ProfileResponse reject(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam UUID sitterId,
            @RequestBody RejectRequest request
    ) {
        return approvals.reject(JwtUser.id(jwt), sitterId, request.reason());
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

    @GetMapping("/sitters/map")
    List<ListedSitterResponse> listedSittersForMap(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(name = "petType", required = false) List<String> petTypes,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "") String experience
    ) {
        return approvals.searchListedForMap(keyword, petTypes, minRating, experience);
    }

    @GetMapping("/sitters/{sitterId}")
    PublicSitterDetailResponse publicSitter(@PathVariable UUID sitterId) {
        return approvals.publicDetail(sitterId);
    }

    @GetMapping("/sitters/{sitterId}/reviews")
    List<PublicReviewResponse> publicReviews(@PathVariable UUID sitterId) {
        return approvals.publicReviews(sitterId);
    }
}
