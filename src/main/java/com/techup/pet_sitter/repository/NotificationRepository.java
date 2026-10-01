package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUser_IdAndIsReadFalse(UUID userId);
    Optional<Notification> findByIdAndUser_Id(Long id, UUID userId);
}
