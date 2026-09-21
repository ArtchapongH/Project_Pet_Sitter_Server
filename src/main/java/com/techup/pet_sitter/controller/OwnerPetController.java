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

    @GetMapping
    public List<PetResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return pets.list(JwtUser.id(jwt));
    }

    @GetMapping("/{id}")
    public PetResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return pets.get(JwtUser.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PetRequest request) {
        return pets.create(JwtUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    public PetResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody PetRequest request
    ) {
        return pets.update(JwtUser.id(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        pets.delete(JwtUser.id(jwt), id);
    }
}
