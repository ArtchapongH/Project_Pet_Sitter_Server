package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.BankAccountPayload;
import com.techup.pet_sitter.dto.PayoutResponse;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.repository.BookingRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PayoutService {
    private final BookingRepository bookings;
    private final SitterProfileRepository profiles;

    public PayoutService(BookingRepository bookings, SitterProfileRepository profiles) {
        this.bookings = bookings;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public PayoutResponse get(UUID sitterId) {
        SitterProfile profile = requireProfile(sitterId);
        var transactions = bookings.findBySitter_UserIdOrderByCreatedAtDesc(sitterId).stream()
                .filter(b -> "success".equals(b.getStatus()))
                .map(b -> new PayoutResponse.Transaction(b.getId(), b.getUpdatedAt(), b.getOwner().getName(),
                        b.getTransactionNo(), b.getTotalPrice()))
                .toList();
        BigDecimal total = transactions.stream().map(PayoutResponse.Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PayoutResponse(total, bank(profile), transactions);
    }

    @Transactional
    public BankAccountPayload updateBank(UUID sitterId, BankAccountPayload payload) {
        if (payload.bankName() == null || payload.bankName().isBlank() || payload.accountName() == null
                || payload.accountName().isBlank() || payload.accountNumber() == null || payload.accountNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bank account details are incomplete");
        }
        SitterProfile profile = requireProfile(sitterId);
        profile.setBankName(payload.bankName().trim());
        profile.setAccountName(payload.accountName().trim());
        profile.setAccountNumber(payload.accountNumber().trim());
        profile.setBankCode(payload.bankCode());
        profile.setBookBankImageUrl(payload.bookBankImageUrl());
        return bank(profiles.save(profile));
    }

    private SitterProfile requireProfile(UUID sitterId) {
        return profiles.findForUpdate(sitterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sitter profile not found"));
    }

    private BankAccountPayload bank(SitterProfile p) {
        return new BankAccountPayload(p.getBankName(), p.getAccountName(), p.getAccountNumber(),
                p.getBankCode(), p.getBookBankImageUrl());
    }
}
