package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByOwner_IdAndSitter_UserId(UUID ownerId, UUID sitterId);

    @Query("""
        SELECT c FROM Conversation c
        JOIN FETCH c.owner
        JOIN FETCH c.sitter s
        JOIN FETCH s.user
        WHERE c.owner.id = :userId OR s.userId = :userId
        ORDER BY c.createdAt DESC
        """)
    List<Conversation> findForUser(@Param("userId") UUID userId);

    @Query("""
        SELECT c FROM Conversation c
        JOIN FETCH c.owner
        JOIN FETCH c.sitter s
        JOIN FETCH s.user
        WHERE c.id = :id AND (c.owner.id = :userId OR s.userId = :userId)
        """)
    Optional<Conversation> findForUser(@Param("id") Long id, @Param("userId") UUID userId);
}
