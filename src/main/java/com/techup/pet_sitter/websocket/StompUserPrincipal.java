package com.techup.pet_sitter.websocket;

import java.security.Principal;
import java.util.UUID;

public final class StompUserPrincipal implements Principal {
    private final String userId;

    public StompUserPrincipal(UUID userId) {
        this.userId = userId.toString();
    }

    @Override
    public String getName() {
        return userId;
    }
}
