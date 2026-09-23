package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.dto.BookingPetRow;
import com.techup.pet_sitter.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBySitter_UserIdOrderByCreatedAtDesc(UUID sitterId);
    Optional<Booking> findByIdAndSitter_UserId(Long id, UUID sitterId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.owner " +
            "WHERE b.sitter.userId = :sitterId " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findBySitterId(@Param("sitterId") UUID sitterId);

    @Query("SELECT new com.techup.pet_sitter.dto.BookingPetRow(" +
            "bp.booking.id, p.id, p.name, pt.name, p.avatarUrl) " +
            "FROM BookingPet bp JOIN bp.pet p JOIN p.petType pt " +
            "WHERE bp.booking.sitter.userId = :sitterId")
    List<BookingPetRow> findPetRowsBySitterId(@Param("sitterId") UUID sitterId);
}
