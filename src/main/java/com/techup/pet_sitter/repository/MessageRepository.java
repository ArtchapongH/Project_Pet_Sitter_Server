package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.conversation.id = :conversationId ORDER BY m.sentAt")
    List<Message> findThread(@Param("conversationId") Long conversationId);

    Optional<Message> findTopByConversation_IdOrderBySentAtDesc(Long conversationId);

    long countByConversation_IdAndReadAtIsNullAndSender_IdNot(Long conversationId, UUID userId);

    List<Message> findByConversation_IdAndReadAtIsNullAndSender_IdNot(Long conversationId, UUID userId);
}
