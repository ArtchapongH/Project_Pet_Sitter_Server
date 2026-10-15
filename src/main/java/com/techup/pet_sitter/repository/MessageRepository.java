package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findFirstByConversation_IdOrderByIdDesc(Long conversationId);

    long countByConversation_IdAndSender_IdNotAndReadAtIsNull(Long conversationId, UUID userId);

    @Query("""
            SELECT m FROM Message m
            JOIN FETCH m.sender
            WHERE m.conversation.id = :conversationId
            AND (:beforeId IS NULL OR m.id < :beforeId)
            ORDER BY m.id DESC
            """)
    List<Message> findPage(
            @Param("conversationId") Long conversationId,
            @Param("beforeId") Long beforeId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Message m
            SET m.readAt = :readAt
            WHERE m.conversation.id = :conversationId
            AND m.sender.id <> :readerId
            AND m.readAt IS NULL
            """)
    int markUnreadAsRead(
            @Param("conversationId") Long conversationId,
            @Param("readerId") UUID readerId,
            @Param("readAt") OffsetDateTime readAt
    );

    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.conversation.id = :conversationId ORDER BY m.sentAt")
    List<Message> findThread(@Param("conversationId") Long conversationId);

    Optional<Message> findTopByConversation_IdOrderBySentAtDesc(Long conversationId);

    long countByConversation_IdAndReadAtIsNullAndSender_IdNot(Long conversationId, UUID userId);

    List<Message> findByConversation_IdAndReadAtIsNullAndSender_IdNot(Long conversationId, UUID userId);
}
