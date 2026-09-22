package com.techup.pet_sitter.repository;

<<<<<<< HEAD
import com.techup.pet_sitter.dto.ReviewAdminListItem;
import com.techup.pet_sitter.entity.Review;
=======
import com.techup.pet_sitter.entity.Review;
import org.springframework.data.domain.Pageable;
>>>>>>> dev
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long> {
<<<<<<< HEAD

    @Query("SELECT new com.techup.pet_sitter.dto.ReviewAdminListItem(" +
            "r.id, o.name, o.avatarUrl, r.createdAt, r.rating, r.comment) " +
            "FROM Review r JOIN r.owner o " +
            "WHERE r.sitter.userId = :sitterId " +
            "ORDER BY r.createdAt DESC")
    List<ReviewAdminListItem> findAdminListBySitterId(@Param("sitterId") UUID sitterId);
=======
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
>>>>>>> dev
}
