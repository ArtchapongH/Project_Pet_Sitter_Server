package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.PetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PetTypeRepository extends JpaRepository<PetType, Integer> {
    Optional<PetType> findByName(String name);
}
