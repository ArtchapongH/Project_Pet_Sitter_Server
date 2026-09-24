package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.AuthBootstrapRequest;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthSyncServiceTest {
    @Test
    void normalizesLegacySitterRoleToPetSitter() {
        UserRepository users = mock(UserRepository.class);
        SitterProfileRepository profiles = mock(SitterProfileRepository.class);
        AuthSyncService service = new AuthSyncService(users, profiles);
        UUID userId = UUID.randomUUID();
        AtomicReference<User> savedUser = new AtomicReference<>();

        when(users.findById(userId)).thenReturn(Optional.empty());
        when(users.findByEmailIgnoreCase("sitter@example.com")).thenReturn(Optional.empty());
        when(users.existsByPhone("0812345678")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(call -> {
            User user = call.getArgument(0);
            savedUser.set(user);
            return user;
        });
        when(profiles.existsById(userId)).thenReturn(false, true);
        when(profiles.save(any(SitterProfile.class))).thenAnswer(call -> call.getArgument(0));

        var result = service.bootstrap(
                userId,
                "sitter@example.com",
                new AuthBootstrapRequest("Sitter", "0812345678", "sitter")
        );

        assertEquals("pet-sitter", result.role());
        assertEquals("pet-sitter", savedUser.get().getRole());
    }
}
