package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.BookingReschedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRescheduleRepository extends JpaRepository<BookingReschedule, Long> {
}
