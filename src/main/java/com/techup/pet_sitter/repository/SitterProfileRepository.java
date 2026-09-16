package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.SitterProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SitterProfileRepository extends JpaRepository<SitterProfile, UUID> {
    List<SitterProfile> findByApprovalStatusIn(Collection<String> statuses);
    List<SitterProfile> findByIsListedTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from SitterProfile profile where profile.userId = :userId")
    Optional<SitterProfile> findForUpdate(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select profile from SitterProfile profile where profile.userId = :userId")
    Optional<SitterProfile> findForBooking(@Param("userId") UUID userId);
}
