package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.BookingAdminListItem;
import com.techup.pet_sitter.dto.BookingPetInfo;
import com.techup.pet_sitter.dto.BookingPetRow;
import com.techup.pet_sitter.entity.Booking;
import com.techup.pet_sitter.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingAdminService {

    private final BookingRepository bookings;

    public BookingAdminService(BookingRepository bookings) {
        this.bookings = bookings;
    }

    public List<BookingAdminListItem> listForSitter(UUID sitterId) {
        List<Booking> rows = bookings.findBySitterId(sitterId);
        Map<Long, List<BookingPetInfo>> petsByBooking = bookings.findPetRowsBySitterId(sitterId).stream()
                .collect(Collectors.groupingBy(BookingPetRow::bookingId,
                        Collectors.mapping(row -> new BookingPetInfo(row.petId(), row.name(), row.type(), row.avatarUrl()),
                                Collectors.toList())));

        return rows.stream()
                .map(b -> new BookingAdminListItem(
                        b.getId(),
                        b.getOwner().getName(),
                        b.getTotalPrice(),
                        b.getDuration(),
                        b.getDurationUnit(),
                        b.getStartDate(),
                        b.getStartTime(),
                        b.getEndDate(),
                        b.getEndTime(),
                        b.getStatus(),
                        petsByBooking.getOrDefault(b.getId(), List.of())))
                .toList();
    }
}
