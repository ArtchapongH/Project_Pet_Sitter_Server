package com.techup.pet_sitter.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupabaseStorageServiceTest {
    @Test
    void jpgMimeIsSentAsJpegAndUserJwtIsPreferred() {
        assertEquals("image/jpeg", SupabaseStorageService.storageType("image/jpg"));
        assertEquals("image/jpeg", SupabaseStorageService.storageType(null));
        assertEquals("user.access.token", SupabaseStorageService.authorizationBearer("sb_secret_key", "user.access.token"));
        assertEquals("header.payload.sig", SupabaseStorageService.authorizationBearer("header.payload.sig", null));
        assertEquals(
                "Image upload is not allowed by the Supabase Storage policy",
                SupabaseStorageService.storageErrorMessage(403)
        );
        assertEquals(
                "Could not upload the image to Supabase Storage (status 500)",
                SupabaseStorageService.storageErrorMessage(500)
        );
    }
}
