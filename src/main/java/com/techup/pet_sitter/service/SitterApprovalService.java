package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.ListedSitterResponse;
import com.techup.pet_sitter.dto.ListedSitterSearchResponse;
import com.techup.pet_sitter.dto.ProfilePayload;
import com.techup.pet_sitter.dto.ProfileResponse;
import com.techup.pet_sitter.dto.PublicReviewResponse;
import com.techup.pet_sitter.dto.PublicSitterDetailResponse;
import com.techup.pet_sitter.entity.PetType;
import com.techup.pet_sitter.entity.SitterPetType;
import com.techup.pet_sitter.entity.SitterPhoto;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.PetTypeRepository;
import com.techup.pet_sitter.repository.ReviewRepository;
import com.techup.pet_sitter.repository.SitterPetTypeRepository;
import com.techup.pet_sitter.repository.SitterPhotoRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SitterApprovalService {
    private static final Set<String> ALLOWED_PET_TYPES = Set.of("Dog", "Cat", "Bird", "Rabbit");

    private final UserRepository users;
    private final SitterProfileRepository profiles;
    private final PetTypeRepository petTypes;
    private final SitterPetTypeRepository sitterPetTypes;
    private final SitterPhotoRepository photos;
    private final ReviewRepository reviews;
    private final ObjectMapper json;

    public SitterApprovalService(
            UserRepository users,
            SitterProfileRepository profiles,
            PetTypeRepository petTypes,
            SitterPetTypeRepository sitterPetTypes,
            SitterPhotoRepository photos,
            ReviewRepository reviews,
            ObjectMapper json
    ) {
        this.users = users;
        this.profiles = profiles;
        this.petTypes = petTypes;
        this.sitterPetTypes = sitterPetTypes;
        this.photos = photos;
        this.reviews = reviews;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getOwnProfile(UUID userId) {
        User user = requireUser(userId);
        return profiles.findById(userId)
                .map(this::response)
                .orElseGet(() -> new ProfileResponse(
                        userId,
                        ApprovalStatus.UNVERIFIED.value(),
                        false,
                        null,
                        basicUserPayload(user),
                        null
                ));
    }

    @Transactional
    public ProfileResponse submit(UUID userId, ProfilePayload payload) {
        User user = requireUser(userId);
        SitterProfile profile = profiles.findForUpdate(userId).orElseGet(() -> newProfile(user));
        ApprovalStatus current = ApprovalStatus.from(profile.getApprovalStatus());
        validate(payload, current == ApprovalStatus.UNVERIFIED);

        ApprovalStatus next;
        try {
            next = current.afterSubmit();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }

        profile.setPendingProfile(write(payload));
        profile.setApprovalStatus(next.value());
        profile.setRejectionReason(null);
        if (current != ApprovalStatus.APPROVED) profile.setListed(false);
        return response(profiles.save(profile));
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> approvalQueue(UUID adminId) {
        requireAdmin(adminId);
        return profiles.findByApprovalStatusIn(List.of(
                        ApprovalStatus.WAITING_FOR_VERIFY.value(),
                        ApprovalStatus.WAITING_FOR_APPROVE.value()
                )).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfileResponse adminProfile(UUID adminId, UUID sitterId) {
        requireAdmin(adminId);
        return profiles.findByIdWithUser(sitterId)
                .map(this::response)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sitter profile not found"));
    }

    @Transactional
    public ProfileResponse approve(UUID adminId, UUID sitterId) {
        requireAdmin(adminId);
        SitterProfile profile = requireProfileForUpdate(sitterId);
        ApprovalStatus current = ApprovalStatus.from(profile.getApprovalStatus());
        ProfilePayload pending = readRequired(profile.getPendingProfile());

        ApprovalStatus next;
        try {
            next = current.afterApprove();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }

        if (current == ApprovalStatus.WAITING_FOR_VERIFY) {
            applyBasic(profile, pending);
            profile.getUser().setVerified(true);
        } else {
            applyAll(profile, pending);
        }
        profile.setApprovalStatus(next.value());
        profile.setListed(next == ApprovalStatus.APPROVED);
        profile.setPendingProfile(null);
        profile.setRejectionReason(null);
        return response(profiles.save(profile));
    }

    @Transactional
    public ProfileResponse reject(UUID adminId, UUID sitterId, String reason) {
        requireAdmin(adminId);
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }
        SitterProfile profile = requireProfileForUpdate(sitterId);
        ApprovalStatus next;
        try {
            next = ApprovalStatus.from(profile.getApprovalStatus()).afterReject();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
        profile.setApprovalStatus(next.value());
        profile.setListed(false);
        profile.setRejectionReason(reason.trim());
        return response(profiles.save(profile));
    }

    @Transactional(readOnly = true)
    public List<ListedSitterResponse> listedProfiles() {
        return profiles.findByIsListedTrue().stream()
                .map(this::listedResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ListedSitterSearchResponse searchListed(
            String keyword,
            List<String> petTypes,
            BigDecimal minRating,
            String experience,
            Integer page,
            Integer limit
    ) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null ? 10 : Math.max(1, Math.min(50, limit));
        String safeKeyword = keyword == null ? "" : keyword.trim();
        List<String> safePetTypes = petTypes == null
                ? List.of()
                : petTypes.stream().filter(type -> type != null && !type.isBlank()).distinct().toList();
        String safeExperience = experience == null ? "" : experience.trim();

        validateRating(minRating);

        List<String> experienceValues = experienceValues(safeExperience);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit);
        Page<SitterProfile> result = profiles.searchListed(
                safeKeyword,
                !safePetTypes.isEmpty(),
                safePetTypes.isEmpty() ? List.of("") : safePetTypes,
                minRating,
                safeExperience,
                !safeExperience.isEmpty(),
                experienceValues,
                pageable
        );

        return new ListedSitterSearchResponse(
                result.getContent().stream().map(this::listedResponse).toList(),
                result.getNumber() + 1,
                result.getTotalPages(),
                result.getTotalElements(),
                safeLimit
        );
    }

    @Transactional(readOnly = true)
    public List<ListedSitterResponse> searchListedForMap(
            String keyword,
            List<String> petTypes,
            BigDecimal minRating,
            String experience
    ) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        List<String> safePetTypes = petTypes == null
                ? List.of()
                : petTypes.stream().filter(type -> type != null && !type.isBlank()).distinct().toList();
        String safeExperience = experience == null ? "" : experience.trim();
        validateRating(minRating);
        List<String> experienceValues = experienceValues(safeExperience);

        return profiles.searchListed(
                        safeKeyword,
                        !safePetTypes.isEmpty(),
                        safePetTypes.isEmpty() ? List.of("") : safePetTypes,
                        minRating,
                        safeExperience,
                        !safeExperience.isEmpty(),
                        experienceValues,
                        Pageable.unpaged()
                ).getContent().stream()
                .map(this::listedResponse)
                .filter(sitter -> sitter.latitude() != null && sitter.longitude() != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicSitterDetailResponse publicDetail(UUID sitterId) {
        SitterProfile profile = requirePublicProfile(sitterId);
        List<String> petTypeNames = sitterPetTypes.findBySitter_UserId(profile.getUserId()).stream()
                .map(link -> link.getPetType().getName())
                .toList();
        List<String> photoUrls = photos.findBySitter_UserIdOrderBySortOrder(profile.getUserId()).stream()
                .map(SitterPhoto::getPhotoUrl)
                .toList();

        return new PublicSitterDetailResponse(
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getUser().getAvatarUrl(),
                profile.getUser().getName(),
                profile.getIntroduction(),
                profile.getServices(),
                profile.getMyPlace(),
                profile.getAddressDetail(),
                profile.getSubDistrict(),
                profile.getDistrict(),
                profile.getProvince(),
                profile.getPostCode(),
                profile.getExperienceYears(),
                petTypeNames,
                photoUrls,
                safeRating(profile),
                safeReviewCount(profile),
                profile.getLatitude(),
                profile.getLongitude()
        );
    }

    @Transactional(readOnly = true)
    public List<PublicReviewResponse> publicReviews(UUID sitterId) {
        requirePublicProfile(sitterId);
        return reviews.findLatestApprovedBySitterId(sitterId, PageRequest.of(0, 5)).stream()
                .map(review -> new PublicReviewResponse(
                        review.getId(),
                        review.getOwner().getName(),
                        review.getOwner().getAvatarUrl(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt()
                ))
                .toList();
    }

    private ListedSitterResponse listedResponse(SitterProfile profile) {
        List<SitterPhoto> profilePhotos = photos.findBySitter_UserIdOrderBySortOrder(profile.getUserId());
        return new ListedSitterResponse(
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getUser().getAvatarUrl(),
                sitterPetTypes.findBySitter_UserId(profile.getUserId()).stream()
                        .map(link -> link.getPetType().getName()).toList(),
                profile.getServices(),
                profile.getIntroduction(),
                profile.getProvince(),
                profile.getUser().getName(),
                profilePhotos.isEmpty() ? null : profilePhotos.get(0).getPhotoUrl(),
                profile.getExperienceYears(),
                safeRating(profile),
                safeReviewCount(profile),
                profile.getLatitude(),
                profile.getLongitude()
        );
    }

    private BigDecimal safeRating(SitterProfile profile) {
        return profile.getRatingAvg() == null ? BigDecimal.ZERO : profile.getRatingAvg();
    }

    private SitterProfile requirePublicProfile(UUID sitterId) {
        return profiles.findByIdWithUser(sitterId)
                .filter(SitterProfile::isListed)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet sitter not found"));
    }

    private int safeReviewCount(SitterProfile profile) {
        return profile.getReviewCount() == null ? 0 : profile.getReviewCount();
    }

    private void validateRating(BigDecimal minRating) {
        if (minRating != null && (minRating.compareTo(BigDecimal.ZERO) < 0
                || minRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 0 and 5");
        }
    }

    private List<String> experienceValues(String experience) {
        return switch (experience) {
            case "" -> List.of("0–1 year", "1–3 years", "3–5 years", "5+ years");
            case "0-2 Years" -> List.of("0–1 year", "1–3 years");
            case "3-5 Years" -> List.of("3–5 years");
            case "5+ Years" -> List.of("5+ years");
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown experience filter");
        };
    }

    @Transactional(readOnly = true)
    public SitterProfile requireBookable(UUID sitterId) {
        SitterProfile profile = profiles.findForBooking(sitterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sitter profile not found"));
        if (!profile.isListed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sitter is not available for booking");
        }
        return profile;
    }

    private SitterProfile newProfile(User user) {
        SitterProfile profile = new SitterProfile();
        profile.setUser(user);
        profile.setUserId(user.getId());
        profile.setDisplayName(user.getName() == null ? "New sitter" : user.getName());
        profile.setExperienceYears("0–1 year");
        profile.setRatingAvg(BigDecimal.ZERO);
        profile.setReviewCount(0);
        profile.setApprovalStatus(ApprovalStatus.UNVERIFIED.value());
        profile.setListed(false);
        return profile;
    }

    private void validate(ProfilePayload payload, boolean firstRound) {
        requireText(payload.fullName(), "Full name");
        requireText(payload.phone(), "Phone");
        requireText(payload.email(), "Email");
        requireText(payload.experienceYears(), "Experience");
        if (payload.dateOfBirth() == null) badRequest("Date of birth is required");
        requireText(payload.idNumber(), "ID number");
        if (firstRound) return;
        requireText(payload.displayName(), "Pet sitter name");
        if (payload.petTypes().isEmpty()) badRequest("At least one pet type is required");
        if (!ALLOWED_PET_TYPES.containsAll(payload.petTypes())) badRequest("Unknown pet type");
        requireText(payload.addressDetail(), "Address detail");
        requireText(payload.district(), "District");
        requireText(payload.subDistrict(), "Sub-district");
        requireText(payload.province(), "Province");
        requireText(payload.postCode(), "Post code");
    }

    private void applyBasic(SitterProfile profile, ProfilePayload payload) {
        User user = profile.getUser();
        user.setName(payload.fullName());
        user.setPhone(payload.phone());
        user.setEmail(payload.email());
        user.setDateOfBirth(payload.dateOfBirth());
        user.setIdNumber(payload.idNumber());
        user.setAvatarUrl(payload.avatarUrl());
        profile.setIntroduction(payload.introduction());
        profile.setExperienceYears(payload.experienceYears());
        users.save(user);
    }

    private void applyAll(SitterProfile profile, ProfilePayload payload) {
        applyBasic(profile, payload);
        profile.setDisplayName(payload.displayName());
        profile.setServices(payload.services());
        profile.setMyPlace(payload.myPlace());
        profile.setAddressDetail(payload.addressDetail());
        profile.setDistrict(payload.district());
        profile.setSubDistrict(payload.subDistrict());
        profile.setProvince(payload.province());
        profile.setPostCode(payload.postCode());
        profile.setLatitude(payload.latitude());
        profile.setLongitude(payload.longitude());
        profile.setBankName(payload.bankName());
        profile.setAccountName(payload.accountName());
        profile.setAccountNumber(payload.accountNumber());
        profile.setBankCode(payload.bankCode());
        profile.setBookBankImageUrl(payload.bookBankImageUrl());
        replacePetTypes(profile, payload.petTypes());
        replacePhotos(profile, payload.photoUrls());
    }

    private void replacePetTypes(SitterProfile profile, List<String> names) {
        List<PetType> matches = new HashSet<>(names).stream()
                .map(name -> petTypes.findByName(name).orElseGet(() -> {
                    PetType petType = new PetType();
                    petType.setName(name);
                    return petTypes.save(petType);
                }))
                .toList();
        sitterPetTypes.deleteBySitter_UserId(profile.getUserId());
        List<SitterPetType> links = matches.stream().map(petType -> {
            SitterPetType link = new SitterPetType();
            link.setId(new SitterPetType.SitterPetTypeId(profile.getUserId(), petType.getId()));
            link.setSitter(profile);
            link.setPetType(petType);
            return link;
        }).toList();
        sitterPetTypes.saveAll(links);
    }

    private void replacePhotos(SitterProfile profile, List<String> urls) {
        photos.deleteBySitter_UserId(profile.getUserId());
        List<SitterPhoto> replacements = new ArrayList<>();
        for (int index = 0; index < urls.size(); index++) {
            SitterPhoto photo = new SitterPhoto();
            photo.setSitter(profile);
            photo.setPhotoUrl(urls.get(index));
            photo.setSortOrder(index);
            replacements.add(photo);
        }
        photos.saveAll(replacements);
    }

    private ProfileResponse response(SitterProfile profile) {
        return new ProfileResponse(
                profile.getUserId(),
                profile.getApprovalStatus(),
                profile.isListed(),
                profile.getRejectionReason(),
                livePayload(profile),
                read(profile.getPendingProfile())
        );
    }

    private ProfilePayload livePayload(SitterProfile profile) {
        User user = profile.getUser();
        List<String> names = sitterPetTypes.findBySitter_UserId(profile.getUserId()).stream()
                .map(link -> link.getPetType().getName())
                .toList();
        List<String> urls = photos.findBySitter_UserIdOrderBySortOrder(profile.getUserId()).stream()
                .map(SitterPhoto::getPhotoUrl)
                .toList();
        return new ProfilePayload(
                user.getName(), user.getPhone(), user.getEmail(), profile.getExperienceYears(),
                user.getDateOfBirth(), user.getIdNumber(), user.getAvatarUrl(), profile.getIntroduction(),
                profile.getDisplayName(), names, profile.getServices(), profile.getMyPlace(), urls,
                profile.getAddressDetail(), profile.getDistrict(), profile.getSubDistrict(), profile.getProvince(),
                profile.getPostCode(), profile.getLatitude(), profile.getLongitude(), profile.getBankName(),
                profile.getAccountName(), profile.getAccountNumber(), profile.getBankCode(), profile.getBookBankImageUrl()
        );
    }

    private ProfilePayload basicUserPayload(User user) {
        return new ProfilePayload(
                user.getName(), user.getPhone(), user.getEmail(), "", user.getDateOfBirth(), user.getIdNumber(),
                user.getAvatarUrl(), null, null, List.of(), null, null, List.of(), null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }

    private User requireUser(UUID userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void requireAdmin(UUID adminId) {
        if (!requireUser(adminId).isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private SitterProfile requireProfileForUpdate(UUID sitterId) {
        return profiles.findForUpdate(sitterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sitter profile not found"));
    }

    private String write(ProfilePayload payload) {
        try {
            return json.writeValueAsString(payload);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot serialize pending profile");
        }
    }

    private ProfilePayload read(String value) {
        if (value == null) return null;
        try {
            return json.readValue(value, ProfilePayload.class);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid pending profile data");
        }
    }

    private ProfilePayload readRequired(String value) {
        ProfilePayload payload = read(value);
        if (payload == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Pending profile is missing");
        return payload;
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) badRequest(field + " is required");
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
