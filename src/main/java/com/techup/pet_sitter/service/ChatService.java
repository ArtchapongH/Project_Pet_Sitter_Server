package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.ConversationPeer;
import com.techup.pet_sitter.dto.ConversationResponse;
import com.techup.pet_sitter.dto.MessagePageResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final UserRepository users;
    private final SitterProfileRepository sitters;
    private final ChatRealtimePublisher realtime;

    public ChatService(
            ConversationRepository conversations,
            MessageRepository messages,
            UserRepository users,
            SitterProfileRepository sitters,
            ChatRealtimePublisher realtime
    ) {
        this.conversations = conversations;
        this.messages = messages;
        this.users = users;
        this.sitters = sitters;
        this.realtime = realtime;
    }

    @Transactional
    public ConversationResponse open(UUID userId, OpenConversationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sitterId is required");
        }
        UUID sitterId = requireId(request.sitterId(), "sitterId is required");
        UUID ownerId = request.ownerId() == null ? userId : request.ownerId();
        if (!userId.equals(ownerId) && !userId.equals(sitterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this conversation");
        }
        if (ownerId.equals(sitterId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot open a conversation with yourself");
        }

        return conversations.findByOwner_IdAndSitter_UserId(ownerId, sitterId)
                .map(existing -> toConversationResponse(requireVisible(existing.getId(), userId), userId))
                .orElseGet(() -> createConversation(userId, ownerId, sitterId));
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list(UUID userId) {
        requireUser(userId);
        return conversations.findVisibleTo(userId).stream()
                .map(conversation -> toConversationResponse(conversation, userId))
                .sorted(Comparator.comparing(this::sortTime).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse get(UUID userId, Long conversationId) {
        return toConversationResponse(requireVisible(conversationId, userId), userId);
    }

    @Transactional(readOnly = true)
    public MessagePageResponse listMessages(UUID userId, Long conversationId, Long beforeId, Integer limit) {
        requireVisible(conversationId, userId);
        int pageSize = normalizeLimit(limit);
        List<Message> rows = messages.findPage(conversationId, beforeId, PageRequest.of(0, pageSize + 1));
        boolean hasMore = rows.size() > pageSize;
        List<Message> page = hasMore ? rows.subList(0, pageSize) : rows;
        List<MessageResponse> items = new ArrayList<>(page.size());
        for (int index = page.size() - 1; index >= 0; index--) {
            items.add(toMessageResponse(page.get(index)));
        }
        return new MessagePageResponse(items, hasMore);
    }

    @Transactional
    public MessageResponse send(UUID userId, Long conversationId, String content, String imageUrl) {
        Conversation conversation = requireVisible(conversationId, userId);
        User sender = requireUser(userId);
        String trimmedContent = trimToNull(content);
        String trimmedImage = trimToNull(imageUrl);
        if (trimmedContent == null && trimmedImage == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content or image is required");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(trimmedContent);
        message.setImageUrl(trimmedImage);
        message.setSentAt(OffsetDateTime.now());
        Message saved = messages.save(message);
        MessageResponse response = toMessageResponse(saved);
        UUID ownerId = conversation.getOwner().getId();
        UUID sitterId = conversation.getSitter().getUserId();
        publishAfterCommit(() -> realtime.publishMessage(response, ownerId, sitterId));
        return response;
    }

    @Transactional
    public void markRead(UUID userId, Long conversationId) {
        Conversation conversation = requireVisible(conversationId, userId);
        OffsetDateTime readAt = OffsetDateTime.now();
        int updated = messages.markUnreadAsRead(conversationId, userId, readAt);
        if (updated > 0) {
            UUID ownerId = conversation.getOwner().getId();
            UUID sitterId = conversation.getSitter().getUserId();
            publishAfterCommit(() -> realtime.publishRead(conversationId, userId, readAt, ownerId, sitterId));
        }
    }

    private ConversationResponse createConversation(UUID userId, UUID ownerId, UUID sitterId) {
        User owner = requireUser(ownerId);
        SitterProfile sitter = sitters.findByIdWithUser(sitterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sitter not found"));
        if (sitter.getUser() != null) {
            OwnerProfileRules.requireNotBanned(sitter.getUser());
        }

        Conversation conversation = new Conversation();
        conversation.setOwner(owner);
        conversation.setSitter(sitter);
        conversation.setCreatedAt(OffsetDateTime.now());
        Conversation saved = conversations.save(conversation);
        return toConversationResponse(requireVisible(saved.getId(), userId), userId);
    }

    private Conversation requireVisible(Long conversationId, UUID userId) {
        requireUser(userId);
        Conversation conversation = conversations.findByIdWithParticipants(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        boolean participant = userId.equals(conversation.getOwner().getId())
                || userId.equals(conversation.getSitter().getUserId());
        if (!participant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this conversation");
        }
        return conversation;
    }

    private User requireUser(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        OwnerProfileRules.requireNotBanned(user);
        return user;
    }

    private ConversationResponse toConversationResponse(Conversation conversation, UUID userId) {
        Long conversationId = conversation.getId();
        MessageResponse lastMessage = messages.findFirstByConversation_IdOrderByIdDesc(conversationId)
                .map(this::toMessageResponse)
                .orElse(null);
        long unreadCount = messages.countByConversation_IdAndSender_IdNotAndReadAtIsNull(conversationId, userId);
        return new ConversationResponse(
                conversationId,
                conversation.getOwner().getId(),
                conversation.getSitter().getUserId(),
                toPeer(conversation, userId),
                lastMessage,
                unreadCount,
                conversation.getCreatedAt()
        );
    }

    private ConversationPeer toPeer(Conversation conversation, UUID userId) {
        boolean currentUserIsOwner = userId.equals(conversation.getOwner().getId());
        if (currentUserIsOwner) {
            SitterProfile sitter = conversation.getSitter();
            User sitterUser = sitter.getUser();
            String name = hasText(sitter.getDisplayName())
                    ? sitter.getDisplayName()
                    : sitterUser == null ? null : sitterUser.getName();
            String avatar = sitterUser == null ? null : sitterUser.getAvatarUrl();
            return new ConversationPeer(sitter.getUserId(), name, avatar, "sitter");
        }
        User owner = conversation.getOwner();
        return new ConversationPeer(owner.getId(), owner.getName(), owner.getAvatarUrl(), "owner");
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getImageUrl(),
                message.getSentAt(),
                message.getReadAt()
        );
    }

    private OffsetDateTime sortTime(ConversationResponse conversation) {
        return conversation.lastMessage() == null ? conversation.createdAt() : conversation.lastMessage().sentAt();
    }

    private void publishAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private static UUID requireId(UUID value, String message) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private static int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(limit, MAX_PAGE_SIZE);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
