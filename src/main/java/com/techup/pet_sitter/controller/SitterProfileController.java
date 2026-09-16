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
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "http://localhost:4173",
    "http://127.0.0.1:4173"
})
public class SitterProfileController {

    @Autowired
    private SitterProfileService sitterProfileService;

    @PostMapping
    public ResponseEntity<SitterProfile> create(@RequestBody SitterProfile sitterProfile) {
        return new ResponseEntity<>(sitterProfileService.create(sitterProfile), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SitterProfile>> getAll() {
        return ResponseEntity.ok(sitterProfileService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SitterProfile> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(sitterProfileService.getById(id));
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
