package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.OwnerAdminDetail;
import com.techup.pet_sitter.service.OwnerAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/owners")
public class OwnerAdminController {

    @Autowired
    private OwnerAdminService ownerAdminService;

    @GetMapping
    public ResponseEntity<OwnerAdminService.OwnerAdminPageResponse> getAll(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(ownerAdminService.getPaginated(keyword, page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerAdminDetail> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ownerAdminService.getDetailById(id));
    }

    @PatchMapping("/{id}/ban")
    public ResponseEntity<OwnerAdminDetail> ban(@PathVariable UUID id) {
        ownerAdminService.setBanned(id, true);
        return ResponseEntity.ok(ownerAdminService.getDetailById(id));
    }

    @PatchMapping("/{id}/unban")
    public ResponseEntity<OwnerAdminDetail> unban(@PathVariable UUID id) {
        ownerAdminService.setBanned(id, false);
        return ResponseEntity.ok(ownerAdminService.getDetailById(id));
    }
}
