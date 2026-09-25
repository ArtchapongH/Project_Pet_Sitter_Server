package com.techup.pet_sitter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
public class SupabaseStorageService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String supabaseUrl;
    private final String bucket;
    private final String apiKey;

    static String storageType(String contentType) {
        if (contentType == null || contentType.isBlank() || "image/jpg".equals(contentType) || "image/pjpeg".equals(contentType)) {
            return "image/jpeg";
        }
        return contentType;
    }

    static String authorizationBearer(String apiKey, String userToken) {
        if (userToken != null && userToken.chars().filter(ch -> ch == '.').count() == 2) return userToken;
        return apiKey;
    }

    private String currentUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) return jwt.getTokenValue();
        return null;
    }

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.bucket}") String bucket,
            @Value("${supabase.apiKey}") String apiKey
    ) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.replaceAll("/$", "");
        this.bucket = bucket;
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    public String uploadImage(UUID ownerId, String folder, MultipartFile file) {
        if (supabaseUrl.isBlank() || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "File storage is not configured");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An image file is required");
        }
        String contentType = storageType(file.getContentType());
        if (!IMAGE_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only jpeg, png, webp or gif images are allowed");
        }
        String extension = switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
        String path = ownerId + "/" + folder + "/" + UUID.randomUUID() + "." + extension;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + authorizationBearer(apiKey, currentUserToken()))
                    .header("apikey", apiKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not upload the image");
            }
            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not upload the image");
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not upload the image");
        }
    }
}
