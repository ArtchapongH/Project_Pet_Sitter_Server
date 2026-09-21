package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBySitter_UserIdOrderByCreatedAtDesc(UUID sitterId);
    Optional<Booking> findByIdAndSitter_UserId(Long id, UUID sitterId);
}
