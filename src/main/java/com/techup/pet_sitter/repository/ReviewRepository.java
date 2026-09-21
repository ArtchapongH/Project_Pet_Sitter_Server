package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
        SELECT review FROM Review review
        JOIN FETCH review.owner
        WHERE review.sitter.userId = :sitterId
          AND review.isApproved = true
        ORDER BY review.createdAt DESC
        """)
    List<Review> findLatestApprovedBySitterId(
            @Param("sitterId") UUID sitterId,
            Pageable pageable
    );
}
