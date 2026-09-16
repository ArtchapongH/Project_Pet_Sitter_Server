package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.SitterPetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SitterPetTypeRepository extends JpaRepository<SitterPetType, SitterPetType.SitterPetTypeId> {
    List<SitterPetType> findBySitter_UserId(UUID sitterId);
    void deleteBySitter_UserId(UUID sitterId);
}
