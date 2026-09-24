package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.SitterProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SitterProfileRepository extends JpaRepository<SitterProfile, UUID> {
    List<SitterProfile> findByApprovalStatusIn(Collection<String> statuses);
    List<SitterProfile> findByIsListedTrue();

    @Query(value = """
        SELECT DISTINCT sp.* FROM sitter_profiles sp
        JOIN users u ON u.id = sp.user_id
        WHERE sp.is_listed = true
        AND (:keyword = ''
            OR LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sp.display_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(sp.services, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(sp.introduction, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(sp.province, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:petTypeFilter = false OR EXISTS (
            SELECT 1 FROM sitter_pet_types spt
            JOIN pet_types pt ON pt.id = spt.pet_type_id
            WHERE spt.sitter_id = sp.user_id AND pt.name IN (:petTypes)
        ))
        AND (:minRating IS NULL OR COALESCE(sp.rating_avg, 0) >= :minRating)
        AND (
            :experienceFilter = false
            OR sp.experience_years IN (:experienceValues)
            OR (
                (:experience = '0-2 Years' AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END < 3)
                OR (:experience = '3-5 Years' AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END >= 3 AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END < 5)
                OR (:experience = '5+ Years' AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END >= 5)
            )
        )
        ORDER BY sp.rating_avg DESC, sp.created_at DESC
        """,
        nativeQuery = true,
        countQuery = """
        SELECT COUNT(DISTINCT sp.user_id) FROM sitter_profiles sp
        JOIN users u ON u.id = sp.user_id
        WHERE sp.is_listed = true
        AND (:keyword = ''
            OR LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(sp.display_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(sp.services, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(sp.introduction, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(COALESCE(sp.province, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:petTypeFilter = false OR EXISTS (
            SELECT 1 FROM sitter_pet_types spt
            JOIN pet_types pt ON pt.id = spt.pet_type_id
            WHERE spt.sitter_id = sp.user_id AND pt.name IN (:petTypes)
        ))
        AND (:minRating IS NULL OR COALESCE(sp.rating_avg, 0) >= :minRating)
        AND (
            :experienceFilter = false
            OR sp.experience_years IN (:experienceValues)
            OR (
                (:experience = '0-2 Years' AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END < 3)
                OR (:experience = '3-5 Years' AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END >= 3 AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END < 5)
                OR (:experience = '5+ Years' AND CASE WHEN sp.experience_years ~ '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sp.experience_years AS NUMERIC) ELSE -1 END >= 5)
            )
        )
        """)
    Page<SitterProfile> searchListed(
            @Param("keyword") String keyword,
            @Param("petTypeFilter") boolean petTypeFilter,
            @Param("petTypes") Collection<String> petTypes,
            @Param("minRating") BigDecimal minRating,
            @Param("experience") String experience,
            @Param("experienceFilter") boolean experienceFilter,
            @Param("experienceValues") Collection<String> experienceValues,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from SitterProfile profile where profile.userId = :userId")
    Optional<SitterProfile> findForUpdate(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select profile from SitterProfile profile where profile.userId = :userId")
    Optional<SitterProfile> findForBooking(@Param("userId") UUID userId);

    // Fetch SitterProfile along with associated User entity
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
        AND sp.pet_sitter_state IN (2, 3)
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
        AND sp.pet_sitter_state IN (2, 3)
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
