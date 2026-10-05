package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.OwnerBookingResponse;
import com.techup.pet_sitter.dto.OwnerReportRequest;
import com.techup.pet_sitter.dto.OwnerReviewRequest;
import com.techup.pet_sitter.dto.OwnerScheduleRequest;
import com.techup.pet_sitter.entity.Booking;
import com.techup.pet_sitter.entity.BookingReschedule;
import com.techup.pet_sitter.entity.Report;
import com.techup.pet_sitter.entity.Review;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.BookingPetRepository;
import com.techup.pet_sitter.repository.BookingRepository;
import com.techup.pet_sitter.repository.BookingRescheduleRepository;
import com.techup.pet_sitter.repository.ReportRepository;
import com.techup.pet_sitter.repository.ReviewRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OwnerBookingService {
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy | h:mm a", Locale.ENGLISH);
    private final BookingRepository bookings;
    private final BookingPetRepository bookingPets;
    private final ReviewRepository reviews;
    private final ReportRepository reports;
    private final BookingRescheduleRepository reschedules;
    private final UserRepository users;
    private final BookingScheduleGuard scheduleGuard;
    private final Clock clock;

    @Autowired
    public OwnerBookingService(BookingRepository bookings, BookingPetRepository bookingPets, ReviewRepository reviews,
                               ReportRepository reports, BookingRescheduleRepository reschedules, UserRepository users,
                               BookingScheduleGuard scheduleGuard) {
        this(bookings, bookingPets, reviews, reports, reschedules, users, scheduleGuard, Clock.system(BANGKOK));
    }

    OwnerBookingService(BookingRepository bookings, BookingPetRepository bookingPets, ReviewRepository reviews,
                        ReportRepository reports, BookingRescheduleRepository reschedules, UserRepository users,
                        BookingScheduleGuard scheduleGuard, Clock clock) {
        this.bookings = bookings;
        this.bookingPets = bookingPets;
        this.reviews = reviews;
        this.reports = reports;
        this.reschedules = reschedules;
        this.users = users;
        this.scheduleGuard = scheduleGuard;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OwnerBookingResponse> list(UUID ownerId) {
        requireOwner(ownerId);
        Map<Long, List<String>> pets = bookingPets.findByOwnerId(ownerId).stream()
                .collect(Collectors.groupingBy(item -> item.getBooking().getId(),
                        Collectors.mapping(item -> item.getPet().getName(), Collectors.toList())));
        Map<Long, Review> reviewByBooking = reviews.findEntitiesByOwnerId(ownerId).stream()
                .collect(Collectors.toMap(review -> review.getBooking().getId(), review -> review, (left, right) -> left));
        return bookings.findOwnerHistory(ownerId).stream()
                .map(booking -> toResponse(booking, pets.getOrDefault(booking.getId(), List.of()), reviewByBooking.get(booking.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OwnerBookingResponse get(UUID ownerId, Long bookingId) {
        requireOwner(ownerId);
        Booking booking = requireOwned(ownerId, bookingId);
        List<String> pets = bookingPets.findByBooking_Id(bookingId).stream().map(item -> item.getPet().getName()).toList();
        Review review = reviews.findByBooking_Id(bookingId).orElse(null);
        return toResponse(booking, pets, review);
    }

    @Transactional
    public OwnerBookingResponse changeSchedule(UUID ownerId, Long bookingId, OwnerScheduleRequest request) {
        requireOwner(ownerId);
        Booking booking = requireOwned(ownerId, bookingId);
        if (!"waiting_confirm".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a booking waiting for confirmation can be changed");
        }
        scheduleGuard.requireAvailable(booking.getSitter(), request.startDate(), request.endDate(), request.startTime(), request.endTime(), booking.getId());
        BookingReschedule history = new BookingReschedule();
        history.setBooking(booking);
        history.setOldDate(booking.getStartDate());
        history.setOldStartTime(booking.getStartTime());
        history.setOldEndTime(booking.getEndTime());
        history.setNewDate(request.startDate());
        history.setNewStartTime(request.startTime());
        history.setNewEndTime(request.endTime());
        history.setChangedAt(OffsetDateTime.now(clock));
        reschedules.save(history);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        bookings.save(booking);
        return get(ownerId, bookingId);
    }

    @Transactional
    public OwnerBookingResponse review(UUID ownerId, Long bookingId, OwnerReviewRequest request) {
        User owner = requireOwner(ownerId);
        Booking booking = requireOwned(ownerId, bookingId);
        if (!"success".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a completed booking can be reviewed");
        }
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        if (reviews.findByBooking_Id(bookingId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking has already been reviewed");
        }
        Review review = new Review();
        review.setBooking(booking);
        review.setOwner(owner);
        review.setSitter(booking.getSitter());
        review.setRating(request.rating().shortValue());
        review.setComment(request.comment() == null ? "" : request.comment().trim());
        review.setCreatedAt(OffsetDateTime.now(clock));
        review.setApproved(true);
        reviews.save(review);
        refreshSitterRating(booking.getSitter());
        return get(ownerId, bookingId);
    }

    @Transactional
    public OwnerBookingResponse report(UUID ownerId, Long bookingId, OwnerReportRequest request) {
        User owner = requireOwner(ownerId);
        Booking booking = requireOwned(ownerId, bookingId);
        if (!"success".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a completed booking can be reported");
        }
        if (request.issue() == null || request.issue().isBlank() || request.description() == null || request.description().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue and description are required");
        }
        if (reports.existsByBooking_Id(bookingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking has already been reported");
        }
        Report report = new Report();
        report.setBooking(booking);
        report.setReporter(owner);
        report.setIssue(request.issue().trim());
        report.setDescription(request.description().trim());
        report.setStatus("pending");
        report.setCreatedAt(OffsetDateTime.now(clock));
        reports.save(report);
        return get(ownerId, bookingId);
    }

    private void refreshSitterRating(SitterProfile sitter) {
        List<Review> all = reviews.findBySitter_UserId(sitter.getUserId());
        sitter.setReviewCount(all.size());
        if (all.isEmpty()) {
            sitter.setRatingAvg(BigDecimal.ZERO);
            return;
        }
        BigDecimal total = all.stream().map(item -> BigDecimal.valueOf(item.getRating())).reduce(BigDecimal.ZERO, BigDecimal::add);
        sitter.setRatingAvg(total.divide(BigDecimal.valueOf(all.size()), 2, RoundingMode.HALF_UP));
    }

    private User requireOwner(UUID ownerId) {
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account profile is missing"));
        OwnerProfileRules.requireNotBanned(owner);
        return owner;
    }

    private Booking requireOwned(UUID ownerId, Long bookingId) {
        return bookings.findOwnerBooking(bookingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private OwnerBookingResponse toResponse(Booking booking, List<String> petNames, Review review) {
        SitterProfile sitter = booking.getSitter();
        User sitterUser = sitter.getUser();
        String status = BookingScheduleRules.ownerStatus(booking.getStatus(), booking.getStartDate(), booking.getStartTime(), clock);
        String place = hasText(sitter.getMyPlace()) ? sitter.getMyPlace() : sitter.getDisplayName();
        return new OwnerBookingResponse(
                booking.getId(),
                sitter.getUserId().toString(),
                place,
                sitterUser.getName() == null ? "" : sitterUser.getName(),
                sitterUser.getAvatarUrl() == null ? "" : sitterUser.getAvatarUrl(),
                status,
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getDuration(),
                durationLabel(booking),
                petNames,
                booking.getTotalPrice(),
                booking.getTransactionNo(),
                DATE.format(booking.getCreatedAt().atZoneSameInstant(BANGKOK)),
                banner(status),
                "completed".equals(status) ? DATE_TIME.format(booking.getUpdatedAt().atZoneSameInstant(BANGKOK)) : null,
                mapQuery(sitter),
                review == null ? null : new OwnerBookingResponse.Review(
                        review.getRating(),
                        review.getComment(),
                        DATE.format(review.getCreatedAt().atZoneSameInstant(BANGKOK))
                )
        );
    }

    private String banner(String status) {
        if ("pending".equals(status)) return "Waiting Pet Sitter for confirm booking";
        if ("in_service".equals(status)) return "Your pet is already in Pet Sitter care!";
        return "";
    }

    private String durationLabel(Booking booking) {
        String amount = booking.getDuration().stripTrailingZeros().toPlainString();
        if ("Day".equals(booking.getDurationUnit())) return amount + ("1".equals(amount) ? " Day" : " Days");
        return amount + ("1".equals(amount) ? " hour" : " hours");
    }

    private String mapQuery(SitterProfile sitter) {
        if (sitter.getLatitude() != null && sitter.getLongitude() != null) {
            return sitter.getLatitude().toPlainString() + "," + sitter.getLongitude().toPlainString();
        }
        return List.of(sitter.getAddressDetail(), sitter.getSubDistrict(), sitter.getDistrict(), sitter.getProvince(), sitter.getPostCode())
                .stream().filter(this::hasText).collect(Collectors.joining(", "));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
