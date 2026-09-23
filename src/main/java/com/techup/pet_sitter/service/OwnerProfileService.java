package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.AuthMeResponse;
import com.techup.pet_sitter.dto.OwnerProfileUpdateRequest;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class OwnerProfileService {
    private final UserRepository users;
    private final SitterProfileRepository sitterProfiles;

    public OwnerProfileService(UserRepository users, SitterProfileRepository sitterProfiles) {
        this.users = users;
        this.sitterProfiles = sitterProfiles;
    }

    @Transactional
    public AuthMeResponse update(UUID userId, OwnerProfileUpdateRequest request) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account profile is missing"));
        OwnerProfileRules.requireNotBanned(user);
        if (users.existsByPhoneAndIdNot(request.phone().trim(), userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This phone number is already registered");
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("owner");
        }
        user.setName(request.name().trim());
        user.setPhone(request.phone().trim());
        user.setIdNumber(blankToNull(request.idNumber()));
        user.setDateOfBirth(request.dateOfBirth());
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
            user.setAvatarUrl(request.avatarUrl().trim());
        }
        return toMe(users.save(user));
    }

    private AuthMeResponse toMe(User user) {
        String role = sitterProfiles.existsById(user.getId()) ? "sitter" : "owner";
        return new AuthMeResponse(
                user.getId(),
                role,
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getIdNumber(),
                user.getDateOfBirth(),
                user.getAvatarUrl(),
                OwnerProfileRules.isComplete(user),
                Boolean.TRUE.equals(user.isBanned())
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
