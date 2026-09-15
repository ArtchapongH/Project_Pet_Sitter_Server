package com.techup.pet_sitter.approval;

import com.techup.pet_sitter.entity.Booking;
import com.techup.pet_sitter.entity.PetType;
import com.techup.pet_sitter.entity.SitterPetType;
import com.techup.pet_sitter.entity.SitterPhoto;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface UserRepository extends JpaRepository<User, UUID> {
}

interface SitterProfileRepository extends JpaRepository<SitterProfile, UUID> {
    List<SitterProfile> findByApprovalStatusIn(Collection<String> statuses);
    List<SitterProfile> findByIsListedTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from SitterProfile profile where profile.userId = :userId")
    Optional<SitterProfile> findForUpdate(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select profile from SitterProfile profile where profile.userId = :userId")
    Optional<SitterProfile> findForBooking(@Param("userId") UUID userId);
}

interface PetTypeRepository extends JpaRepository<PetType, Integer> {
    Optional<PetType> findByName(String name);
}

interface SitterPetTypeRepository extends JpaRepository<SitterPetType, SitterPetType.SitterPetTypeId> {
    List<SitterPetType> findBySitter_UserId(UUID sitterId);
    void deleteBySitter_UserId(UUID sitterId);
}

interface SitterPhotoRepository extends JpaRepository<SitterPhoto, Long> {
    List<SitterPhoto> findBySitter_UserIdOrderBySortOrder(UUID sitterId);
    void deleteBySitter_UserId(UUID sitterId);
}

interface BookingRepository extends JpaRepository<Booking, Long> {
}
