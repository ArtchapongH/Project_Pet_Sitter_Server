package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
