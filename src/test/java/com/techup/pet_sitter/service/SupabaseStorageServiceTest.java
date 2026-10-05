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
    @Test
    void rejectsInvalidImagesBeforeContactingStorage() {
        var storage = new SupabaseStorageService("https://example.invalid", "images", "key");
        var id = java.util.UUID.randomUUID();
        var tooLarge = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(tooLarge.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(tooLarge.getSize()).thenReturn(5_000_001L);
        var oversized = org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> storage.uploadImage(id, "profile", tooLarge));
        assertEquals(400, oversized.getStatusCode().value());
        var text = new org.springframework.mock.web.MockMultipartFile("file", "bad.txt", "text/plain", new byte[]{1});
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> storage.uploadImage(id, "profile", text));
        var empty = new org.springframework.mock.web.MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> storage.uploadImage(id, "profile", empty));
    }
}
