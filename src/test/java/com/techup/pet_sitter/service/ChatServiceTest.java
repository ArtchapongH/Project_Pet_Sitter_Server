package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.ConversationResponse;
import com.techup.pet_sitter.dto.MessageResponse;
import com.techup.pet_sitter.dto.OpenConversationRequest;
import com.techup.pet_sitter.entity.Conversation;
import com.techup.pet_sitter.entity.Message;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.ConversationRepository;
import com.techup.pet_sitter.repository.MessageRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final MessageRepository messages = mock(MessageRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final SitterProfileRepository sitters = mock(SitterProfileRepository.class);
    private final ChatRealtimePublisher realtime = mock(ChatRealtimePublisher.class);
    private final ChatService service = new ChatService(conversations, messages, users, sitters, realtime);

    private final UUID ownerId = UUID.randomUUID();
    private final UUID sitterId = UUID.randomUUID();
    private final UUID strangerId = UUID.randomUUID();

    @Test
    void openRejectsChatWithYourself() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.open(ownerId, new OpenConversationRequest(ownerId, ownerId))
        );
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void openCreatesConversationForOwnerAndSitter() {
        User owner = user(ownerId, "Owner");
        User sitterUser = user(sitterId, "Sitter");
        SitterProfile sitter = sitter(sitterUser);
        when(users.findById(ownerId)).thenReturn(Optional.of(owner));
        when(sitters.findByIdWithUser(sitterId)).thenReturn(Optional.of(sitter));
        when(conversations.findByOwner_IdAndSitter_UserId(ownerId, sitterId)).thenReturn(Optional.empty());
        when(conversations.save(any(Conversation.class))).thenAnswer(call -> {
            Conversation conversation = call.getArgument(0);
            conversation.setId(11L);
            return conversation;
        });
        when(conversations.findByIdWithParticipants(11L)).thenAnswer(call -> Optional.of(conversation(11L, owner, sitter)));
        when(messages.findFirstByConversation_IdOrderByIdDesc(11L)).thenReturn(Optional.empty());
        when(messages.countByConversation_IdAndSender_IdNotAndReadAtIsNull(11L, ownerId)).thenReturn(0L);

        ConversationResponse result = service.open(ownerId, new OpenConversationRequest(sitterId, null));

        assertEquals(11L, result.id());
        assertEquals(ownerId, result.ownerId());
        assertEquals(sitterId, result.sitterId());
        assertEquals("sitter", result.peer().role());
        assertEquals(sitterId, result.peer().userId());
    }

    @Test
    void sendRejectsNonParticipant() {
        User owner = user(ownerId, "Owner");
        User sitterUser = user(sitterId, "Sitter");
        when(users.findById(strangerId)).thenReturn(Optional.of(user(strangerId, "Other")));
        when(conversations.findByIdWithParticipants(5L)).thenReturn(Optional.of(conversation(5L, owner, sitter(sitterUser))));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.send(strangerId, 5L, "hello", null)
        );
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(messages, never()).save(any(Message.class));
    }

    @Test
    void sendPublishesSavedMessage() {
        User owner = user(ownerId, "Owner");
        User sitterUser = user(sitterId, "Sitter");
        Conversation conversation = conversation(5L, owner, sitter(sitterUser));
        when(users.findById(ownerId)).thenReturn(Optional.of(owner));
        when(conversations.findByIdWithParticipants(5L)).thenReturn(Optional.of(conversation));
        when(messages.save(any(Message.class))).thenAnswer(call -> {
            Message message = call.getArgument(0);
            message.setId(99L);
            return message;
        });

        MessageResponse result = service.send(ownerId, 5L, "hello", null);

        assertEquals(99L, result.id());
        assertEquals("hello", result.content());
        verify(realtime).publishMessage(eq(result), eq(ownerId), eq(sitterId));
    }

    private Conversation conversation(Long id, User owner, SitterProfile sitter) {
        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setOwner(owner);
        conversation.setSitter(sitter);
        return conversation;
    }

    private SitterProfile sitter(User user) {
        SitterProfile profile = new SitterProfile();
        profile.setUserId(user.getId());
        profile.setUser(user);
        profile.setDisplayName(user.getName());
        return profile;
    }

    private User user(UUID id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@example.com");
        user.setBanned(false);
        return user;
    }
}
