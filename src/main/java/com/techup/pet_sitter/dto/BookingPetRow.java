package com.techup.pet_sitter.dto;

// flat projection used to group booking_pets + pets rows by booking before building BookingAdminListItem
public record BookingPetRow(
        Long bookingId,
        Long petId,
        String name,
        String type,
        String avatarUrl
) {
}
