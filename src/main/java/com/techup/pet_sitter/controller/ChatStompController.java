package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.StompReadRequest;
import com.techup.pet_sitter.dto.StompSendMessageRequest;
import com.techup.pet_sitter.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatStompController {
    private final ChatService chats;

    public ChatStompController(ChatService chats) {
        this.chats = chats;
    }

    @MessageMapping("/chat.send")
    public void send(StompSendMessageRequest request, Principal principal) {
        requireRequest(request);
        if (request.conversationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conversationId is required");
        }
        chats.send(userId(principal), request.conversationId(), request.content(), request.imageUrl());
    }

    @MessageMapping("/chat.read")
    public void read(StompReadRequest request, Principal principal) {
        requireRequest(request);
        if (request.conversationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "conversationId is required");
        }
        chats.markRead(userId(principal), request.conversationId());
    }

    private static void requireRequest(Object request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
    }

    private static UUID userId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id is required");
        }
        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id is invalid");
        }
    }
}
