package com.techup.pet_sitter.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserIdChannelInterceptorTest {
    private final UserIdChannelInterceptor interceptor = new UserIdChannelInterceptor();

    @Test
    void connectBindsUserFromHeader() {
        UUID userId = UUID.randomUUID();
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("X-User-Id", userId.toString());
        accessor.setLeaveMutable(true);
        Message<byte[]> incoming = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(incoming, null);

        assertEquals(userId.toString(), accessor.getUser().getName());
    }

    @Test
    void connectRequiresUserHeader() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        Message<byte[]> incoming = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(incoming, null));
    }
}
