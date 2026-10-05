package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.AuthBootstrapRequest;
import com.techup.pet_sitter.dto.AuthMeResponse;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AuthSyncService {
    private final UserRepository users;
    private final SitterProfileRepository sitterProfiles;

    public AuthSyncService(UserRepository users, SitterProfileRepository sitterProfiles) {
        this.users = users;
        this.sitterProfiles = sitterProfiles;
    }

    @Transactional
    public AuthMeResponse bootstrap(UUID userId, String email, AuthBootstrapRequest request) {
        User user = users.findById(userId).orElseGet(User::new);
        boolean creating = user.getId() == null;
        if (creating) {
            user.setId(userId);
            user.setAdmin(false);
            user.setVerified(false);
            user.setBanned(false);
        }
        String accountRole = "sitter".equals(request.role()) ? "sitter" : "owner";
        if (creating || user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(accountRole);
        }
        users.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered");
            }
        });
        if (request.phone() != null && !request.phone().isBlank()) {
            boolean phoneTaken = creating
                    ? users.existsByPhone(request.phone())
                    : users.existsByPhoneAndIdNot(request.phone(), userId);
            if (phoneTaken) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This phone number is already registered");
            }
            user.setPhone(request.phone().trim());
        }
        user.setEmail(email);
        user.setName(request.name().trim());
        User saved = users.save(user);
        if ("sitter".equals(request.role()) && !sitterProfiles.existsById(saved.getId())) {
            sitterProfiles.save(newSitterProfile(saved));
        }
        OwnerProfileRules.requireNotBanned(saved);
        return toMe(saved);
    }

    @Transactional(readOnly = true)
    public AuthMeResponse me(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account profile is missing"));
        OwnerProfileRules.requireNotBanned(user);
        return toMe(user);
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
                Boolean.TRUE.equals(user.isBanned()),
                user.isAdmin()
        );
    }

    private SitterProfile newSitterProfile(User user) {
        SitterProfile profile = new SitterProfile();
        profile.setUser(user);
        profile.setUserId(user.getId());
        profile.setDisplayName(user.getName() == null || user.getName().isBlank() ? "New sitter" : user.getName());
        profile.setExperienceYears("0–1 year");
        profile.setRatingAvg(BigDecimal.ZERO);
        profile.setReviewCount(0);
        profile.setApprovalStatus(ApprovalStatus.UNVERIFIED.value());
        profile.setListed(false);
        return profile;
    }
}
