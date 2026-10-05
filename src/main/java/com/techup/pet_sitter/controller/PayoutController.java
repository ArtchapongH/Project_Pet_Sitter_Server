package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.BankAccountPayload;
import com.techup.pet_sitter.dto.PayoutResponse;
import com.techup.pet_sitter.service.PayoutService;
import com.techup.pet_sitter.security.JwtUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/sitter/payout")
public class PayoutController {
    private final PayoutService payouts;
    public PayoutController(PayoutService payouts) { this.payouts = payouts; }

    @GetMapping
    PayoutResponse get(@AuthenticationPrincipal Jwt jwt) { return payouts.get(JwtUser.id(jwt)); }

    @PutMapping("/bank-account")
    BankAccountPayload updateBank(@AuthenticationPrincipal Jwt jwt,
                                  @RequestBody BankAccountPayload payload) {
        return payouts.updateBank(JwtUser.id(jwt), payload);
    }
}
