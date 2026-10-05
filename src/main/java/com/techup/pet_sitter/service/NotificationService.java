package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.NotificationResponse;
import com.techup.pet_sitter.entity.Notification;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId) {
        return notifications.findByUser_IdOrderByCreatedAtDesc(userId).stream().map(this::response).toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID userId, Long id) {
        Notification notification = notifications.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        return response(notifications.save(notification));
    }

    @Transactional
    public void markAllRead(UUID userId) {
        List<Notification> unread = notifications.findByUser_IdAndIsReadFalse(userId);
        unread.forEach(notification -> notification.setRead(true));
        notifications.saveAll(unread);
    }

    public void notify(User user, String type, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setContent(content);
        notification.setRead(false);
        notification.setCreatedAt(OffsetDateTime.now());
        notifications.save(notification);
    }

    private NotificationResponse response(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getContent(),
                notification.isRead(), notification.getCreatedAt());
    }
}
