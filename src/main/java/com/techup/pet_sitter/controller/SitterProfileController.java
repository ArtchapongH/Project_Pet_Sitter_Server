package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.service.SitterProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sitterprofile")
public class SitterProfileController {

    @Autowired
    private SitterProfileService sitterProfileService;

    @PostMapping
    public ResponseEntity<SitterProfile> create(@RequestBody SitterProfile sitterProfile) {
        return new ResponseEntity<>(sitterProfileService.create(sitterProfile), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<SitterProfileService.SitterProfilePageResponse> getAll(
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(sitterProfileService.getPaginated(status, keyword, page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SitterProfileService.SitterProfileDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(sitterProfileService.getDetailById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SitterProfile> update(@PathVariable UUID id, @RequestBody SitterProfile sitterProfile) {
        return ResponseEntity.ok(sitterProfileService.update(id, sitterProfile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sitterProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
