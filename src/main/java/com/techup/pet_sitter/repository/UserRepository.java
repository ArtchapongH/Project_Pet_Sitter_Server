package com.techup.pet_sitter.repository;


import com.techup.pet_sitter.dto.OwnerAdminListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.techup.pet_sitter.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // ==========================================
    // Pet owner admin list: users joined with pet count (mirrors SitterProfileRepository)
    // ==========================================

    @Query("""
        SELECT new com.techup.pet_sitter.dto.OwnerAdminListItem(
            u.id, u.name, u.phone, u.email,
            (SELECT COUNT(p) FROM Pet p WHERE p.owner.id = u.id),
            u.isBanned, u.avatarUrl)
        FROM User u
            WHERE u.role = 'owner'
        AND (
            :keyword = ''
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY u.createdAt DESC
        """)
    List<OwnerAdminListItem> searchOwners(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT COUNT(u) FROM User u
            WHERE u.role = 'owner'
        AND (
            :keyword = ''
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        """)
    long countOwners(@Param("keyword") String keyword);
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, UUID id);
}