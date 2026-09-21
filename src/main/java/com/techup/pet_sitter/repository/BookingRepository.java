package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.dto.BookingAdminListItem;
import com.techup.pet_sitter.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT new com.techup.pet_sitter.dto.BookingAdminListItem(" +
            "b.id, o.name, (SELECT COUNT(bp) FROM BookingPet bp WHERE bp.booking = b), " +
            "b.duration, b.durationUnit, b.startDate, b.startTime, b.status) " +
            "FROM Booking b JOIN b.owner o " +
            "WHERE b.sitter.userId = :sitterId " +
            "ORDER BY b.createdAt DESC")
    List<BookingAdminListItem> findAdminListBySitterId(@Param("sitterId") UUID sitterId);
}
