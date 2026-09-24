package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.PetRequest;
import com.techup.pet_sitter.dto.PetResponse;
import com.techup.pet_sitter.security.JwtUser;
import com.techup.pet_sitter.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner/pets")
public class OwnerPetController {
    private final PetService pets;

    public OwnerPetController(PetService pets) {
        this.pets = pets;
    }

    private static final java.util.UUID DEFAULT_OWNER_ID = java.util.UUID.fromString("9b4e7c12-6f35-4a89-bd21-83c5e7f0496a");

    private java.util.UUID resolveOwnerId(Jwt jwt, java.util.UUID headerUserId) {
        if (jwt != null) {
            return JwtUser.id(jwt);
        }
        if (headerUserId != null) {
            return headerUserId;
        }
        return DEFAULT_OWNER_ID;
    }

    @GetMapping
    public List<PetResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) java.util.UUID headerUserId
    ) {
        return pets.list(resolveOwnerId(jwt, headerUserId));
    }

    @GetMapping("/{id}")
    public PetResponse get(
            @AuthenticationPrincipal Jwt jwt,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) java.util.UUID headerUserId,
            @PathVariable Long id
    ) {
        return pets.get(resolveOwnerId(jwt, headerUserId), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) java.util.UUID headerUserId,
            @Valid @RequestBody PetRequest request
    ) {
        return pets.create(resolveOwnerId(jwt, headerUserId), request);
    }

    @PutMapping("/{id}")
    public PetResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) java.util.UUID headerUserId,
            @PathVariable Long id,
            @Valid @RequestBody PetRequest request
    ) {
        return pets.update(resolveOwnerId(jwt, headerUserId), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) java.util.UUID headerUserId,
            @PathVariable Long id
    ) {
        pets.delete(resolveOwnerId(jwt, headerUserId), id);
    }
}
