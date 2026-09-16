package com.techup.pet_sitter.repository;

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
}
