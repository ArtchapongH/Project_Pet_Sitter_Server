package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.SendMessageRequest;
import com.techup.pet_sitter.entity.Conversation;
import com.techup.pet_sitter.entity.Message;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.ConversationRepository;
import com.techup.pet_sitter.repository.MessageRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageServiceTest {
    @Test
    void sendsMessageAndNotifiesTheOtherParticipant() {
        ConversationRepository conversations = mock(ConversationRepository.class);
        MessageRepository messages = mock(MessageRepository.class);
        UserRepository users = mock(UserRepository.class);
        NotificationService notifications = mock(NotificationService.class);
        UUID ownerId = UUID.randomUUID(), sitterId = UUID.randomUUID();
        User owner = user(ownerId, "Owner"), sitterUser = user(sitterId, "Sitter");
        SitterProfile sitter = new SitterProfile();
        sitter.setUser(sitterUser);
        Conversation conversation = new Conversation();
        conversation.setId(7L);
        conversation.setOwner(owner);
        conversation.setSitter(sitter);
        when(conversations.findForUser(7L, ownerId)).thenReturn(Optional.of(conversation));
        when(users.findById(ownerId)).thenReturn(Optional.of(owner));
        when(messages.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(11L);
            return message;
        });

        var result = new MessageService(conversations, messages, users, mock(SitterProfileRepository.class), notifications)
                .send(ownerId, 7L, new SendMessageRequest("Hello", null));

        assertEquals("Hello", result.content());
        assertTrue(result.mine());
        verify(notifications).notify(sitterUser, "message", "Owner sent you a message");
    }

    private User user(UUID id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }
}
