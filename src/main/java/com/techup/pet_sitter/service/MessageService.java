package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.ConversationResponse;
import com.techup.pet_sitter.dto.MessageResponse;
import com.techup.pet_sitter.dto.SendMessageRequest;
import com.techup.pet_sitter.entity.Conversation;
import com.techup.pet_sitter.entity.Message;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.ConversationRepository;
import com.techup.pet_sitter.repository.MessageRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final UserRepository users;
    private final SitterProfileRepository sitters;
    private final NotificationService notifications;

    public MessageService(ConversationRepository conversations, MessageRepository messages, UserRepository users,
                          SitterProfileRepository sitters, NotificationService notifications) {
        this.conversations = conversations;
        this.messages = messages;
        this.users = users;
        this.sitters = sitters;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list(UUID userId) {
        return conversations.findForUser(userId).stream().map(c -> summary(c, userId)).toList();
    }

    @Transactional
    public ConversationResponse start(UUID ownerId, UUID sitterId) {
        if (sitterId == null || ownerId.equals(sitterId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid pet sitter is required");
        }
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));
        if (!"owner".equals(owner.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only pet owners can start a conversation");
        }
        SitterProfile sitter = sitters.findByIdWithUser(sitterId)
                .filter(SitterProfile::isListed)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet sitter not found"));
        Conversation conversation = conversations.findByOwner_IdAndSitter_UserId(ownerId, sitterId).orElseGet(() -> {
            Conversation created = new Conversation();
            created.setOwner(owner);
            created.setSitter(sitter);
            created.setCreatedAt(OffsetDateTime.now());
            return conversations.save(created);
        });
        return summary(conversation, ownerId);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> thread(UUID userId, Long conversationId) {
        requireParticipant(userId, conversationId);
        return messages.findThread(conversationId).stream().map(m -> response(m, userId)).toList();
    }

    @Transactional
    public MessageResponse send(UUID userId, Long conversationId, SendMessageRequest request) {
        String content = request == null || request.content() == null ? "" : request.content().trim();
        String imageUrl = request == null || request.imageUrl() == null ? "" : request.imageUrl().trim();
        if (content.isEmpty() && imageUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is required");
        }
        Conversation conversation = requireParticipant(userId, conversationId);
        User sender = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content.isEmpty() ? null : content);
        message.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
        message.setSentAt(OffsetDateTime.now());
        Message saved = messages.save(message);
        User recipient = conversation.getOwner().getId().equals(userId)
                ? conversation.getSitter().getUser()
                : conversation.getOwner();
        notifications.notify(recipient, "message", sender.getName() + " sent you a message");
        return response(saved, userId);
    }

    @Transactional
    public void markRead(UUID userId, Long conversationId) {
        requireParticipant(userId, conversationId);
        List<Message> unread = messages.findByConversation_IdAndReadAtIsNullAndSender_IdNot(conversationId, userId);
        OffsetDateTime now = OffsetDateTime.now();
        unread.forEach(message -> message.setReadAt(now));
        messages.saveAll(unread);
    }

    private Conversation requireParticipant(UUID userId, Long conversationId) {
        return conversations.findForUser(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private ConversationResponse summary(Conversation conversation, UUID userId) {
        User participant = conversation.getOwner().getId().equals(userId)
                ? conversation.getSitter().getUser()
                : conversation.getOwner();
        Message latest = messages.findTopByConversation_IdOrderBySentAtDesc(conversation.getId()).orElse(null);
        String lastMessage = latest == null ? "Start a conversation" : latest.getContent();
        return new ConversationResponse(conversation.getId(), participant.getId(), participant.getName(),
                participant.getAvatarUrl(), lastMessage == null ? "Image" : lastMessage,
                messages.countByConversation_IdAndReadAtIsNullAndSender_IdNot(conversation.getId(), userId));
    }

    private MessageResponse response(Message message, UUID userId) {
        return new MessageResponse(message.getId(), message.getConversation().getId(), message.getSender().getId(),
                message.getContent(), message.getImageUrl(), message.getSentAt(), message.getSender().getId().equals(userId));
    }
}
