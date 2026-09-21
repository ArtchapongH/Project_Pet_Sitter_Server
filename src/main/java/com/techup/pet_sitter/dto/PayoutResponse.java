package com.techup.pet_sitter.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PayoutResponse(BigDecimal totalEarning, BankAccountPayload bankAccount,
                             List<Transaction> transactions) {
    public record Transaction(Long bookingId, OffsetDateTime completedAt, String ownerName,
                              String transactionNo, BigDecimal amount) {}
}
