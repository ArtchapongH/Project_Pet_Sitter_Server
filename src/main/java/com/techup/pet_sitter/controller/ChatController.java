package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.ChatConversationResponse;
import com.techup.pet_sitter.dto.MessagePageResponse;
import com.techup.pet_sitter.dto.MessageResponse;
import com.techup.pet_sitter.dto.OpenConversationRequest;
import com.techup.pet_sitter.dto.SendMessageRequest;
import com.techup.pet_sitter.dto.UploadResponse;
import com.techup.pet_sitter.service.ChatService;
import com.techup.pet_sitter.service.SupabaseStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ChatController {
    private final ChatService chats;
    private final SupabaseStorageService storage;

    public ChatController(ChatService chats, SupabaseStorageService storage) {
        this.chats = chats;
        this.storage = storage;
    }

    @PostMapping
    public ChatConversationResponse open(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody OpenConversationRequest request
    ) {
        return chats.open(userId, request);
    }

    @GetMapping
    public List<ChatConversationResponse> list(@RequestHeader("X-User-Id") UUID userId) {
        return chats.list(userId);
    }

    @GetMapping("/{id}")
    public ChatConversationResponse get(@RequestHeader("X-User-Id") UUID userId, @PathVariable Long id) {
        return chats.get(userId, id);
    }

    @GetMapping("/{id}/messages")
    public MessagePageResponse listMessages(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable Long id,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(required = false) Integer limit
    ) {
        return chats.listMessages(userId, id, beforeId, limit);
    }

    @PostMapping("/{id}/messages")
    public MessageResponse send(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return chats.send(userId, id, request.content(), request.imageUrl());
    }

    @PostMapping("/{id}/images")
    public UploadResponse uploadImage(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        chats.get(userId, id);
        return new UploadResponse(storage.uploadImage(userId, "chat", file));
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@RequestHeader("X-User-Id") UUID userId, @PathVariable Long id) {
        chats.markRead(userId, id);
    }
}
