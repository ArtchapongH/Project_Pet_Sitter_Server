package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.SitterUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SitterUnavailabilityRepository extends JpaRepository<SitterUnavailability, Long> {
    List<SitterUnavailability> findBySitter_UserId(UUID sitterId);
}
