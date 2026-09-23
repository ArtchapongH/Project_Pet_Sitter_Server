package com.techup.pet_sitter.repository;

<<<<<<< HEAD
import com.techup.pet_sitter.dto.OwnerPetItem;
=======
>>>>>>> dev
import com.techup.pet_sitter.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
<<<<<<< HEAD
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, Long> {

    long countByOwnerId(UUID ownerId);

    @Query("SELECT new com.techup.pet_sitter.dto.OwnerPetItem(" +
            "p.id, p.name, p.breed, p.sex, p.ageMonths, p.avatarUrl, pt.name, p.isSuspended, p.color, p.weightKg, p.about) " +
            "FROM Pet p JOIN p.petType pt " +
            "WHERE p.owner.id = :ownerId " +
            "ORDER BY p.createdAt DESC")
    List<OwnerPetItem> findByOwnerId(@Param("ownerId") UUID ownerId);
=======
import java.util.Optional;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, Long> {
    @Query("select pet from Pet pet join fetch pet.petType where pet.owner.id = :ownerId order by pet.id desc")
    List<Pet> findAllByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("select pet from Pet pet join fetch pet.petType where pet.id = :id and pet.owner.id = :ownerId")
    Optional<Pet> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") UUID ownerId);
>>>>>>> dev
}
