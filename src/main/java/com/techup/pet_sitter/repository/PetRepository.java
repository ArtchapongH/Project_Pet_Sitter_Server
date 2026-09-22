package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.dto.OwnerPetItem;
import com.techup.pet_sitter.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, Long> {

    long countByOwnerId(UUID ownerId);

    @Query("SELECT new com.techup.pet_sitter.dto.OwnerPetItem(" +
            "p.id, p.name, p.breed, p.sex, p.ageMonths, p.avatarUrl, pt.name, p.isSuspended, p.color, p.weightKg, p.about) " +
            "FROM Pet p JOIN p.petType pt " +
            "WHERE p.owner.id = :ownerId " +
            "ORDER BY p.createdAt DESC")
    List<OwnerPetItem> findByOwnerId(@Param("ownerId") UUID ownerId);
}
