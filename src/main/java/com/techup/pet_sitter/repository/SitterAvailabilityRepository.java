package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.SitterAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SitterAvailabilityRepository extends JpaRepository<SitterAvailability, Long> {
    List<SitterAvailability> findBySitter_UserId(UUID sitterId);
}
