package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.PetType;
import com.techup.pet_sitter.entity.SitterPetType;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.SitterPetTypeRepository;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SitterProfileService {

    @Autowired
    private SitterProfileRepository sitterProfileRepository;

    @Autowired
    private SitterPetTypeRepository sitterPetTypeRepository;

    public SitterProfile create(SitterProfile sitterProfile) {
        return sitterProfileRepository.save(sitterProfile);
    }

    public List<SitterProfile> getAll() {
        return sitterProfileRepository.findAllWithUser();
    }

    // ==========================================
    // Search + status filter + pagination (mirrors PostService.getPosts)
    // ==========================================

    public SitterProfilePageResponse getPaginated(String status, String keyword, Integer page, Integer limit) {

        int safePage = (page == null || page < 1) ? 1 : page;

        int requestedLimit = (limit == null) ? 10 : limit;
        int safeLimit = Math.max(1, Math.min(100, requestedLimit));

        String safeStatus = (status == null) ? "" : status.trim();
        String safeKeyword = (keyword == null) ? "" : keyword.trim();

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit);

        List<SitterProfile> sitters = sitterProfileRepository.searchSitterProfiles(safeStatus, safeKeyword, pageable);
        long totalItems = sitterProfileRepository.countSitterProfiles(safeStatus, safeKeyword);
        int totalPages = (int) Math.ceil((double) totalItems / safeLimit);

        SitterProfilePageResponse response = new SitterProfilePageResponse();
        response.setSitters(sitters);
        response.setCurrentPage(safePage);
        response.setTotalPages(totalPages);
        response.setTotalItems(totalItems);
        response.setLimit(safeLimit);
        return response;
    }

    public static class SitterProfilePageResponse {
        private List<SitterProfile> sitters;
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private int limit;

        public List<SitterProfile> getSitters() {
            return sitters;
        }

        public void setSitters(List<SitterProfile> sitters) {
            this.sitters = sitters;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public long getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(long totalItems) {
            this.totalItems = totalItems;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

    public SitterProfile getById(UUID id) {
        return sitterProfileRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("SitterProfile not found with id: " + id));
    }

    // ==========================================
    // Join SitterProfile + User + PetType (via SitterPetType bridge)
    // ==========================================

    public SitterProfileDetailResponse getDetailById(UUID id) {
        SitterProfile sitterProfile = getById(id);
        User user = sitterProfile.getUser();
        sitterProfile.setUser(null); // avoid duplicating user in the JSON response (returned separately below)

        List<PetType> petTypes = sitterPetTypeRepository.findBySitterIdWithPetType(id).stream()
                .map(SitterPetType::getPetType)
                .collect(Collectors.toList());

        SitterProfileDetailResponse response = new SitterProfileDetailResponse();
        response.setSitterProfile(sitterProfile);
        response.setUser(user);
        response.setPetTypes(petTypes);
        return response;
    }

    public static class SitterProfileDetailResponse {
        private SitterProfile sitterProfile;
        private User user;
        private List<PetType> petTypes;

        public SitterProfile getSitterProfile() {
            return sitterProfile;
        }

        public void setSitterProfile(SitterProfile sitterProfile) {
            this.sitterProfile = sitterProfile;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public List<PetType> getPetTypes() {
            return petTypes;
        }

        public void setPetTypes(List<PetType> petTypes) {
            this.petTypes = petTypes;
        }
    }

    public SitterProfile update(UUID id, SitterProfile updated) {
        SitterProfile existing = getById(id);
        existing.setDisplayName(updated.getDisplayName());
        existing.setIntroduction(updated.getIntroduction());
        existing.setMyPlace(updated.getMyPlace());
        existing.setServices(updated.getServices());
        existing.setAddressDetail(updated.getAddressDetail());
        existing.setDistrict(updated.getDistrict());
        existing.setSubDistrict(updated.getSubDistrict());
        existing.setProvince(updated.getProvince());
        existing.setPostCode(updated.getPostCode());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setExperienceYears(updated.getExperienceYears());
        existing.setBankName(updated.getBankName());
        existing.setAccountNumber(updated.getAccountNumber());
        existing.setAccountName(updated.getAccountName());
        existing.setBankCode(updated.getBankCode());
        existing.setBookBankImageUrl(updated.getBookBankImageUrl());
        return sitterProfileRepository.save(existing);
    }

    public void delete(UUID id) {
        SitterProfile existing = getById(id);
        sitterProfileRepository.delete(existing);
    }
}
