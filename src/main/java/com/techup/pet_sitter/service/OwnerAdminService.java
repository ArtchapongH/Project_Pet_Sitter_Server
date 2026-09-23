package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.OwnerAdminDetail;
import com.techup.pet_sitter.dto.OwnerAdminListItem;
import com.techup.pet_sitter.entity.Pet;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.PetRepository;
import com.techup.pet_sitter.repository.ReviewRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class OwnerAdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public OwnerAdminPageResponse getPaginated(String keyword, Integer page, Integer limit) {
        int safePage = (page == null || page < 1) ? 1 : page;

        int requestedLimit = (limit == null) ? 10 : limit;
        int safeLimit = Math.max(1, Math.min(100, requestedLimit));

        String safeKeyword = (keyword == null) ? "" : keyword.trim();

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit);

        List<OwnerAdminListItem> owners = userRepository.searchOwners(safeKeyword, pageable);
        long totalItems = userRepository.countOwners(safeKeyword);
        int totalPages = (int) Math.ceil((double) totalItems / safeLimit);

        OwnerAdminPageResponse response = new OwnerAdminPageResponse();
        response.setOwners(owners);
        response.setCurrentPage(safePage);
        response.setTotalPages(totalPages);
        response.setTotalItems(totalItems);
        response.setLimit(safeLimit);
        return response;
    }

    public OwnerAdminDetail getDetailById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet owner not found"));

        return new OwnerAdminDetail(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getIdNumber(),
                user.getDateOfBirth(),
                user.getAvatarUrl(),
                user.isBanned(),
                petRepository.findByOwnerId(id),
                reviewRepository.findByOwnerId(id)
        );
    }

    public User setBanned(UUID id, boolean banned) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet owner not found"));
        user.setBanned(banned);
        return userRepository.save(user);
    }

    public Pet suspendPet(UUID ownerId, Long petId) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet owner not found");
        }

        Pet pet = petRepository.findById(petId)
                .filter(foundPet -> foundPet.getOwner().getId().equals(ownerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found for this owner"));
        pet.setIsSuspended(true);
        return petRepository.save(pet);
    }

    public static class OwnerAdminPageResponse {
        private List<OwnerAdminListItem> owners;
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private int limit;

        public List<OwnerAdminListItem> getOwners() {
            return owners;
        }

        public void setOwners(List<OwnerAdminListItem> owners) {
            this.owners = owners;
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
}
