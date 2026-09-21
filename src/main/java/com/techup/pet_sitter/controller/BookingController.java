package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.BookingRequest;
import com.techup.pet_sitter.dto.BookingResponse;
import com.techup.pet_sitter.dto.BookingStatusRequest;
import com.techup.pet_sitter.dto.SitterBookingResponse;
import com.techup.pet_sitter.entity.Booking;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.BookingRepository;
import com.techup.pet_sitter.repository.UserRepository;
import com.techup.pet_sitter.service.OwnerProfileRules;
import com.techup.pet_sitter.service.SitterApprovalService;
import com.techup.pet_sitter.service.SitterBookingService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingRepository bookings;
    private final UserRepository users;
    private final SitterApprovalService approvals;
    private final SitterBookingService sitterBookings;

    public BookingController(BookingRepository bookings, UserRepository users, SitterApprovalService approvals,
                             SitterBookingService sitterBookings) {
        this.bookings = bookings;
        this.users = users;
        this.approvals = approvals;
        this.sitterBookings = sitterBookings;
    }

    @GetMapping("/sitter")
    List<SitterBookingResponse> listForSitter(@RequestHeader("X-User-Id") UUID sitterId,
                                              @RequestParam(required = false) String query,
                                              @RequestParam(required = false) LocalDate from,
                                              @RequestParam(required = false) LocalDate to) {
        return sitterBookings.list(sitterId, query, from, to);
    }

    @GetMapping("/sitter/{id}")
    SitterBookingResponse getForSitter(@RequestHeader("X-User-Id") UUID sitterId, @PathVariable Long id) {
        return sitterBookings.get(sitterId, id);
    }

    @PatchMapping("/sitter/{id}/status")
    SitterBookingResponse changeStatus(@RequestHeader("X-User-Id") UUID sitterId, @PathVariable Long id,
                                       @RequestBody BookingStatusRequest request) {
        return sitterBookings.changeStatus(sitterId, id, request.status());
    }

    @PostMapping
    @Transactional
    BookingResponse create(
            @RequestHeader("X-User-Id") UUID ownerId,
            @RequestBody BookingRequest request
    ) {
        if (request.sitterId() == null || request.startDate() == null || request.endDate() == null
                || request.startTime() == null || request.endTime() == null || request.duration() == null
                || request.totalPrice() == null || request.contactName() == null || request.contactEmail() == null
                || request.contactPhone() == null || !List.of("hours", "Day").contains(request.durationUnit())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking details are incomplete");
        }
        if (request.endDate().isBefore(request.startDate()) || request.duration().signum() <= 0
                || request.totalPrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking dates, duration or price are invalid");
        }
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));
        OwnerProfileRules.requireNotBanned(owner);
        OwnerProfileRules.requireForBooking(owner);
        Booking booking = new Booking();
        booking.setOwner(owner);
        booking.setSitter(approvals.requireBookable(request.sitterId()));
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setDuration(request.duration());
        booking.setDurationUnit(request.durationUnit());
        booking.setContactName(request.contactName());
        booking.setContactEmail(request.contactEmail());
        booking.setContactPhone(request.contactPhone());
        booking.setAdditionalMessage(request.additionalMessage());
        booking.setTotalPrice(request.totalPrice());
        booking.setPaymentMethod(request.paymentMethod());
        booking.setStatus("waiting_confirm");
        Booking saved = bookings.save(booking);
        return new BookingResponse(saved.getId(), saved.getStatus());
    }
}
