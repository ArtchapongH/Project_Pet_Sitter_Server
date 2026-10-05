package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.BankAccountPayload;
import com.techup.pet_sitter.dto.PayoutResponse;
import com.techup.pet_sitter.dto.UploadResponse;
import com.techup.pet_sitter.service.PayoutService;
import com.techup.pet_sitter.service.SupabaseStorageService;
import com.techup.pet_sitter.security.JwtUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/sitter/payout")
public class PayoutController {
    private final PayoutService payouts;
    private final SupabaseStorageService storage;
    private final com.techup.pet_sitter.service.SitterApprovalService approvals;

    public PayoutController(PayoutService payouts, SupabaseStorageService storage, com.techup.pet_sitter.service.SitterApprovalService approvals) {
        this.payouts = payouts;
        this.storage = storage;
        this.approvals = approvals;
    }

    @GetMapping
    PayoutResponse get(@AuthenticationPrincipal Jwt jwt) { return payouts.get(JwtUser.id(jwt)); }

    @PutMapping("/bank-account")
    BankAccountPayload updateBank(@AuthenticationPrincipal Jwt jwt,
                                  @RequestBody BankAccountPayload payload) {
        return payouts.updateBank(JwtUser.id(jwt), payload);
    }

    @PostMapping("/book-bank-image")
    UploadResponse uploadBookBankImage(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam("file") MultipartFile file) {
        approvals.requireSitter(JwtUser.id(jwt));
        return new UploadResponse(storage.uploadImage(JwtUser.id(jwt), "payout", file));
    }
}
