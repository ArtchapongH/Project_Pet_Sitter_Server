package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.BookingPet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingPetRepository extends JpaRepository<BookingPet, BookingPet.BookingPetId> {
    List<BookingPet> findByBooking_Id(Long bookingId);
}
