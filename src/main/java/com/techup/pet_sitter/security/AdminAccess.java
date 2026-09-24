package com.techup.pet_sitter.security;

import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("adminAccess")
public class AdminAccess {
    private final UserRepository users;

    public AdminAccess(UserRepository users) {
        this.users = users;
    }

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        String subject = jwt.getSubject();
        if (subject == null) {
            return false;
        }
        try {
            UUID userId = UUID.fromString(subject);
            return users.findById(userId).map(User::isAdmin).orElse(false);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
