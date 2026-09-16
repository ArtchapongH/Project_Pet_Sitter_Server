package com.techup.pet_sitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.techup.pet_sitter.entity.SitterPetType;

import java.util.List;
import java.util.UUID;

public interface SitterPetTypeRepository extends JpaRepository<SitterPetType, Long> {

    @Query("SELECT spt FROM SitterPetType spt JOIN FETCH spt.petType WHERE spt.id.sitterId = :sitterId")
    List<SitterPetType> findBySitterIdWithPetType(@Param("sitterId") UUID sitterId);
}