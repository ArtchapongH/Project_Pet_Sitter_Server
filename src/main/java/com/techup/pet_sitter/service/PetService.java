package com.techup.pet_sitter.service;

import com.techup.pet_sitter.dto.PetRequest;
import com.techup.pet_sitter.dto.PetResponse;
import com.techup.pet_sitter.entity.Pet;
import com.techup.pet_sitter.entity.PetType;
import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.PetRepository;
import com.techup.pet_sitter.repository.PetTypeRepository;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PetService {
    private static final Set<String> ALLOWED_TYPES = Set.of("Dog", "Cat", "Bird", "Rabbit");
    private final PetRepository pets;
    private final PetTypeRepository petTypes;
    private final UserRepository users;

    public PetService(PetRepository pets, PetTypeRepository petTypes, UserRepository users) {
        this.pets = pets;
        this.petTypes = petTypes;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> list(UUID ownerId) {
        return pets.findAllByOwnerId(ownerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PetResponse get(UUID ownerId, Long petId) {
        return toResponse(requireOwned(ownerId, petId));
    }

    @Transactional
    public PetResponse create(UUID ownerId, PetRequest request) {
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account profile is missing"));
        Pet pet = new Pet();
        pet.setOwner(owner);
        apply(pet, request);
        return toResponse(pets.save(pet));
    }

    @Transactional
    public PetResponse update(UUID ownerId, Long petId, PetRequest request) {
        Pet pet = requireOwned(ownerId, petId);
        apply(pet, request);
        return toResponse(pets.save(pet));
    }

    @Transactional
    public void delete(UUID ownerId, Long petId) {
        pets.delete(requireOwned(ownerId, petId));
    }

    private void apply(Pet pet, PetRequest request) {
        pet.setName(request.name().trim());
        pet.setPetType(requireType(request.petType()));
        pet.setBreed(request.breed().trim());
        pet.setSex(request.sex());
        pet.setAgeMonths(request.ageMonths());
        pet.setColor(request.color().trim());
        pet.setWeightKg(request.weightKg());
        pet.setAbout(request.about() == null ? "" : request.about().trim());
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
            pet.setAvatarUrl(request.avatarUrl().trim());
        }
    }

    private PetType requireType(String name) {
        if (!ALLOWED_TYPES.contains(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown pet type");
        }
        return petTypes.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pet type is not available yet"));
    }

    private Pet requireOwned(UUID ownerId, Long petId) {
        return pets.findByIdAndOwnerId(petId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
    }

    private PetResponse toResponse(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getPetType().getName(),
                pet.getBreed(),
                pet.getSex(),
                pet.getAgeMonths(),
                pet.getColor(),
                pet.getWeightKg(),
                pet.getAbout(),
                pet.getAvatarUrl()
        );
    }
}
