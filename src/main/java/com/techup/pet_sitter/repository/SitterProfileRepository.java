package com.techup.pet_sitter.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.techup.pet_sitter.entity.SitterProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SitterProfileRepository extends JpaRepository<SitterProfile, UUID> {

    @Query("SELECT sp FROM SitterProfile sp JOIN FETCH sp.user")
    List<SitterProfile> findAllWithUser();

    @Query("SELECT sp FROM SitterProfile sp JOIN FETCH sp.user WHERE sp.userId = :id")
    Optional<SitterProfile> findByIdWithUser(@Param("id") UUID id);

    // ==========================================
    // Search + status filter + pagination (mirrors PostRepository)
    // ==========================================

    @Query("""
        SELECT sp FROM SitterProfile sp
        JOIN FETCH sp.user u
        WHERE (:status = '' OR sp.approvalStatus = :status)
        AND (
            :keyword = ''
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sp.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY sp.createdAt DESC
        """)
    List<SitterProfile> searchSitterProfiles(
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(sp) FROM SitterProfile sp
        JOIN sp.user u
        WHERE (:status = '' OR sp.approvalStatus = :status)
        AND (
            :keyword = ''
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sp.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        """)
    long countSitterProfiles(
            @Param("status") String status,
            @Param("keyword") String keyword
    );
}
