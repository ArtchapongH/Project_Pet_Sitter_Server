package com.techup.pet_sitter.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.UUID;

public class UserIdChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(new StompUserPrincipal(readUserId(accessor)));
        }
        return message;
    }

    private UUID readUserId(StompHeaderAccessor accessor) {
        String raw = accessor.getFirstNativeHeader("X-User-Id");
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("X-User-Id is required");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("X-User-Id is invalid");
        }
    }
}
