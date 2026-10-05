package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.Notification;
import com.techup.pet_sitter.repository.NotificationRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationServiceTest {
    @Test
    void marksOnlyTheSignedInUsersNotificationAsRead() {
        NotificationRepository repository = mock(NotificationRepository.class);
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(4L);
        notification.setType("message");
        notification.setContent("New message");
        notification.setCreatedAt(OffsetDateTime.now());
        when(repository.findByIdAndUser_Id(4L, userId)).thenReturn(Optional.of(notification));
        when(repository.save(notification)).thenReturn(notification);

        var result = new NotificationService(repository).markRead(userId, 4L);

        assertTrue(result.read());
    }
}
