package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.NotificationResponse;
import com.techup.pet_sitter.security.JwtUser;
import com.techup.pet_sitter.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    List<NotificationResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return notifications.list(JwtUser.id(jwt));
    }

    @PatchMapping("/{id}/read")
    NotificationResponse read(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return notifications.markRead(JwtUser.id(jwt), id);
    }

    @PatchMapping("/read-all")
    ResponseEntity<Void> readAll(@AuthenticationPrincipal Jwt jwt) {
        notifications.markAllRead(JwtUser.id(jwt));
        return ResponseEntity.noContent().build();
    }
}
