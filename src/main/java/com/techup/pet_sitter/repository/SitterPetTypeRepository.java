package com.techup.pet_sitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techup.pet_sitter.entity.SitterPetType;

public interface SitterPetTypeRepository extends JpaRepository<SitterPetType, Long> {
}