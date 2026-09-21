package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.BankAccountPayload;
import com.techup.pet_sitter.dto.PayoutResponse;
import com.techup.pet_sitter.service.PayoutService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/sitter/payout")
public class PayoutController {
    private final PayoutService payouts;
    public PayoutController(PayoutService payouts) { this.payouts = payouts; }

    @GetMapping
    PayoutResponse get(@RequestHeader("X-User-Id") UUID sitterId) { return payouts.get(sitterId); }

    @PutMapping("/bank-account")
    BankAccountPayload updateBank(@RequestHeader("X-User-Id") UUID sitterId,
                                  @RequestBody BankAccountPayload payload) {
        return payouts.updateBank(sitterId, payload);
    }
}
