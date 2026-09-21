package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.ProfilePayload;
import com.techup.pet_sitter.dto.ProfileResponse;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.PetTypeRepository;
import com.techup.pet_sitter.repository.ReviewRepository;
import com.techup.pet_sitter.repository.SitterPetTypeRepository;
import com.techup.pet_sitter.repository.SitterPhotoRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SitterApprovalServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final SitterProfileRepository profiles = mock(SitterProfileRepository.class);
    private final PetTypeRepository petTypes = mock(PetTypeRepository.class);
    private final SitterPetTypeRepository sitterPetTypes = mock(SitterPetTypeRepository.class);
    private final SitterPhotoRepository photos = mock(SitterPhotoRepository.class);
    private final ReviewRepository reviews = mock(ReviewRepository.class);
    private final ObjectMapper json = mock(ObjectMapper.class);
    private final SitterApprovalService service = new SitterApprovalService(
            users, profiles, petTypes, sitterPetTypes, photos, reviews, json
    );
    private final UUID sitterId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void repositoryDefaults() {
        when(sitterPetTypes.findBySitter_UserId(sitterId)).thenReturn(List.of());
        when(photos.findBySitter_UserIdOrderBySortOrder(sitterId)).thenReturn(List.of());
        when(profiles.save(any(SitterProfile.class))).thenAnswer(call -> call.getArgument(0));
        User admin = new User();
        admin.setId(adminId);
        admin.setAdmin(true);
        when(users.findById(adminId)).thenReturn(Optional.of(admin));
    }

    @Test
    void firstSubmissionWaitsForVerificationWithoutChangingLiveUser() {
        User sitter = user();
        when(users.findById(sitterId)).thenReturn(Optional.of(sitter));
        when(profiles.findForUpdate(sitterId)).thenReturn(Optional.empty());
        when(json.writeValueAsString(any(ProfilePayload.class))).thenReturn("{}");
        when(json.readValue("{}", ProfilePayload.class)).thenReturn(basicPayload());

        ProfileResponse result = service.submit(sitterId, basicPayload());

        assertEquals("Waiting for verify", result.approvalStatus());
        assertFalse(result.listed());
        assertEquals("old name", sitter.getName());
        assertEquals("new name", result.pendingProfile().fullName());
    }

    @Test
    void firstApprovalPromotesBasicDataAndClearsPending() {
        User sitter = user();
        SitterProfile profile = profile(sitter, "Waiting for verify", false);
        profile.setPendingProfile("{}");
        when(profiles.findForUpdate(sitterId)).thenReturn(Optional.of(profile));
        when(json.readValue("{}", ProfilePayload.class)).thenReturn(basicPayload());

        ProfileResponse result = service.approve(adminId, sitterId);

        assertEquals("Verified", result.approvalStatus());
        assertFalse(result.listed());
        assertEquals("new name", sitter.getName());
        assertTrue(sitter.isVerified());
        assertNull(profile.getPendingProfile());
    }

    @Test
    void rejectionKeepsLiveDataAndRemovesListing() {
        User sitter = user();
        SitterProfile profile = profile(sitter, "Waiting for approve", true);
        profile.setPendingProfile("{}");
        when(profiles.findForUpdate(sitterId)).thenReturn(Optional.of(profile));
        when(json.readValue("{}", ProfilePayload.class)).thenReturn(basicPayload());

        ProfileResponse result = service.reject(adminId, sitterId, "revise address");

        assertEquals("Rejected", result.approvalStatus());
        assertFalse(result.listed());
        assertEquals("old name", sitter.getName());
        assertEquals("{}", profile.getPendingProfile());
        assertEquals("revise address", result.rejectionReason());
    }

    @Test
    void unlistedSitterCannotReceiveNewBooking() {
        SitterProfile profile = profile(user(), "Rejected", false);
        when(profiles.findForBooking(sitterId)).thenReturn(Optional.of(profile));
        assertThrows(ResponseStatusException.class, () -> service.requireBookable(sitterId));
        profile.setListed(true);
        assertEquals(profile, service.requireBookable(sitterId));
    }

    @Test
    void publicDetailOnlyReturnsListedProfileWithExactCoordinates() {
        SitterProfile profile = profile(user(), "Approved", true);
        profile.setLatitude(new BigDecimal("13.75630000"));
        profile.setLongitude(new BigDecimal("100.50180000"));
        when(profiles.findByIdWithUser(sitterId)).thenReturn(Optional.of(profile));

        var result = service.publicDetail(sitterId);

        assertEquals(new BigDecimal("13.75630000"), result.latitude());
        assertEquals(new BigDecimal("100.50180000"), result.longitude());
        profile.setListed(false);
        assertThrows(ResponseStatusException.class, () -> service.publicDetail(sitterId));
    }

    private User user() {
        User sitter = new User();
        sitter.setId(sitterId);
        sitter.setName("old name");
        sitter.setEmail("old@example.com");
        return sitter;
    }

    private SitterProfile profile(User sitter, String status, boolean listed) {
        SitterProfile profile = new SitterProfile();
        profile.setUserId(sitterId);
        profile.setUser(sitter);
        profile.setDisplayName("old sitter");
        profile.setExperienceYears("0–1 year");
        profile.setApprovalStatus(status);
        profile.setListed(listed);
        return profile;
    }

    private ProfilePayload basicPayload() {
        return new ProfilePayload(
                "new name", "0812345678", "new@example.com", "0–1 year", LocalDate.of(1990, 1, 1),
                "1234567890123", null, "intro", null, List.of(), null, null, List.of(), null,
                null, null, null, null, null, null, null, null, null, null, null
        );
    }
}
