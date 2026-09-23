package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.dto.ReportAdminListItem;
import com.techup.pet_sitter.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // User = the booking's sitter (sitter_profiles.user_id = users.id), Reported Person = the booking's owner
    @Query("SELECT new com.techup.pet_sitter.dto.ReportAdminListItem(" +
            "r.id, b.sitter.user.name, b.owner.name, r.issue, r.description, r.createdAt, r.status) " +
            "FROM Report r JOIN r.booking b " +
            "ORDER BY r.createdAt DESC")
    List<ReportAdminListItem> findAdminList();

    @Query("SELECT new com.techup.pet_sitter.dto.ReportAdminListItem(" +
            "r.id, b.sitter.user.name, b.owner.name, r.issue, r.description, r.createdAt, r.status) " +
            "FROM Report r JOIN r.booking b " +
            "WHERE r.id = :id")
    Optional<ReportAdminListItem> findAdminDetailById(@Param("id") Long id);
}
