package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.BookingPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookingPetRepository extends JpaRepository<BookingPet, BookingPet.BookingPetId> {
    List<BookingPet> findByBooking_Id(Long bookingId);

    @Query("""
            SELECT bookingPet FROM BookingPet bookingPet
            JOIN FETCH bookingPet.pet
            WHERE bookingPet.booking.owner.id = :ownerId
            """)
    List<BookingPet> findByOwnerId(@Param("ownerId") UUID ownerId);
}
