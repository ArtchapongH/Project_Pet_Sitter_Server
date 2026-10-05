package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.ConversationResponse;
import com.techup.pet_sitter.dto.MessageResponse;
import com.techup.pet_sitter.dto.SendMessageRequest;
import com.techup.pet_sitter.dto.StartConversationRequest;
import com.techup.pet_sitter.security.JwtUser;
import com.techup.pet_sitter.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messages;

    public MessageController(MessageService messages) {
        this.messages = messages;
    }

    @GetMapping("/conversations")
    List<ConversationResponse> conversations(@AuthenticationPrincipal Jwt jwt) {
        return messages.list(JwtUser.id(jwt));
    }

    @PostMapping("/conversations")
    ConversationResponse start(@AuthenticationPrincipal Jwt jwt, @RequestBody StartConversationRequest request) {
        return messages.start(JwtUser.id(jwt), request.sitterId());
    }

    @GetMapping("/conversations/{id}")
    List<MessageResponse> thread(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return messages.thread(JwtUser.id(jwt), id);
    }

    @PostMapping("/conversations/{id}")
    MessageResponse send(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                         @RequestBody SendMessageRequest request) {
        return messages.send(JwtUser.id(jwt), id, request);
    }

    @PatchMapping("/conversations/{id}/read")
    ResponseEntity<Void> read(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        messages.markRead(JwtUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }
}
