package com.techup.pet_sitter.dto;

public record BookingResponse(Long id, String status, String transactionNo) {
    public BookingResponse(Long id, String status) {
        this(id, status, null);
    }
}
