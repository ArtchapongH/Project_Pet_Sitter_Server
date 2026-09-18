package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.SitterPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SitterPhotoRepository extends JpaRepository<SitterPhoto, Long> {
    List<SitterPhoto> findBySitter_UserIdOrderBySortOrder(UUID sitterId);
    void deleteBySitter_UserId(UUID sitterId);
}
