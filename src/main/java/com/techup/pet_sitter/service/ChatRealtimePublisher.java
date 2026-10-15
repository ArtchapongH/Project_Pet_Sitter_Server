package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.ChatEvent;
import com.techup.pet_sitter.dto.MessageResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ChatRealtimePublisher {
    private final SimpMessagingTemplate messaging;

    public ChatRealtimePublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void publishMessage(MessageResponse message, UUID ownerId, UUID sitterId) {
        ChatEvent event = new ChatEvent("message", message.conversationId(), message, null, null);
        messaging.convertAndSend("/topic/conversations/" + message.conversationId(), event);
        sendToUser(ownerId, event);
        sendToUser(sitterId, event);
    }

    public void publishRead(Long conversationId, UUID readerId, OffsetDateTime readAt, UUID ownerId, UUID sitterId) {
        ChatEvent event = new ChatEvent("read", conversationId, null, readerId, readAt);
        messaging.convertAndSend("/topic/conversations/" + conversationId, event);
        sendToUser(ownerId, event);
        sendToUser(sitterId, event);
    }

    private void sendToUser(UUID userId, ChatEvent event) {
        messaging.convertAndSendToUser(userId.toString(), "/queue/chat", event);
    }
}
