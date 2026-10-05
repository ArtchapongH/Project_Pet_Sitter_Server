package com.techup.pet_sitter.controller;

import com.techup.pet_sitter.service.PayoutService;
import com.techup.pet_sitter.service.SitterApprovalService;
import com.techup.pet_sitter.service.SupabaseStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SitterMediaControllerTest {
    private final UUID sitterId = UUID.randomUUID();
    private final Jwt jwt = Jwt.withTokenValue("test").header("alg", "none").subject(sitterId.toString()).build();
    private final MockMultipartFile image = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1});
    private final SitterApprovalService approvals = mock(SitterApprovalService.class);
    private final SupabaseStorageService storage = mock(SupabaseStorageService.class);

    @Test
    void profileAndGalleryReturnStorageUrlAndRejectInvalidFolders() {
        var controller = new SitterApprovalController(approvals, storage);
        for (String folder : new String[]{"profile", "gallery"}) {
            when(storage.uploadImage(sitterId, folder, image)).thenReturn("https://storage/" + folder);
            assertEquals("https://storage/" + folder, controller.uploadProfileMedia(jwt, image, folder).url());
        }
        assertThrows(ResponseStatusException.class, () -> controller.uploadProfileMedia(jwt, image, "../owner"));
        verify(storage, times(2)).uploadImage(eq(sitterId), anyString(), eq(image));
    }

    @Test
    void ownerCannotUploadProfileOrBookBankImage() {
        when(approvals.requireSitter(sitterId)).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));
        assertThrows(ResponseStatusException.class,
                () -> new SitterApprovalController(approvals, storage).uploadProfileMedia(jwt, image, "profile"));
        assertThrows(ResponseStatusException.class,
                () -> new PayoutController(mock(PayoutService.class), storage, approvals).uploadBookBankImage(jwt, image));
        verifyNoInteractions(storage);
    }

    @Test
    void payoutUploadReturnsUrlWithoutChangingBankAccount() {
        var payouts = mock(PayoutService.class);
        when(storage.uploadImage(sitterId, "payout", image)).thenReturn("https://storage/book-bank.png");
        assertEquals("https://storage/book-bank.png",
                new PayoutController(payouts, storage, approvals).uploadBookBankImage(jwt, image).url());
        verify(approvals).requireSitter(sitterId);
        verifyNoInteractions(payouts);
    }
}