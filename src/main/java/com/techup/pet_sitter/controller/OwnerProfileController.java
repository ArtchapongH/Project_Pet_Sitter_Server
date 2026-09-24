package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.AuthMeResponse;
import com.techup.pet_sitter.dto.OwnerProfileUpdateRequest;
import com.techup.pet_sitter.dto.UploadResponse;
import com.techup.pet_sitter.security.JwtUser;
import com.techup.pet_sitter.service.AuthSyncService;
import com.techup.pet_sitter.service.OwnerProfileService;
import com.techup.pet_sitter.service.SupabaseStorageService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/owner")
public class OwnerProfileController {
    private final OwnerProfileService profiles;
    private final AuthSyncService authSync;
    private final SupabaseStorageService storage;

    public OwnerProfileController(
            OwnerProfileService profiles,
            AuthSyncService authSync,
            SupabaseStorageService storage
    ) {
        this.profiles = profiles;
        this.authSync = authSync;
        this.storage = storage;
    }

    @GetMapping("/profile")
    public AuthMeResponse get(@AuthenticationPrincipal Jwt jwt) {
        return authSync.me(JwtUser.id(jwt));
    }

    @PutMapping("/profile")
    public AuthMeResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OwnerProfileUpdateRequest request
    ) {
        return profiles.update(JwtUser.id(jwt), request);
    }

    @PostMapping("/media")
    public UploadResponse upload(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "profile") String folder
    ) {
        String safeFolder = "pet".equals(folder) ? "pets" : "profile";
        return new UploadResponse(storage.uploadImage(JwtUser.id(jwt), safeFolder, file));
    }
}
