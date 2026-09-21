package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.dto.AuthBootstrapRequest;
import com.techup.pet_sitter.dto.AuthMeResponse;
import com.techup.pet_sitter.security.JwtUser;
import com.techup.pet_sitter.service.AuthSyncService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthSyncService authSync;

    public AuthController(AuthSyncService authSync) {
        this.authSync = authSync;
    }

    @PostMapping("/bootstrap")
    public AuthMeResponse bootstrap(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AuthBootstrapRequest request
    ) {
        return authSync.bootstrap(JwtUser.id(jwt), JwtUser.email(jwt), request);
    }

    @GetMapping("/me")
    public AuthMeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authSync.me(JwtUser.id(jwt));
    }
}
