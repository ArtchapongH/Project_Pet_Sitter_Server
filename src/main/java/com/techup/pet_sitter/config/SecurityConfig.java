package com.techup.pet_sitter.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sitters", "/api/sitters/**", "/api/locations").permitAll()
                        .requestMatchers("/api/admin/**", "/api/sitterprofile/**", "/api/reports/**", "/api/reviews/**",
                                "/api/user/**", "/api/bookings/admin/**", "/api/locations/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin-patterns}") String allowedOriginPatterns
    ) {
        List<String> origins = new ArrayList<>();
        origins.add("https://pet-sitter-client-one.vercel.app");
        origins.add("https://*.vercel.app");
        Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .forEach(origins::add);
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origins.stream().distinct().toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${supabase.url}") String supabaseUrl) {
        String base = supabaseUrl == null ? "" : supabaseUrl.replaceAll("/$", "");
        if (base.isBlank()) {
            throw new IllegalStateException("supabase.url is required to verify Supabase Auth tokens");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(base + "/auth/v1/.well-known/jwks.json")
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(base + "/auth/v1"));
        return decoder;
    }
}
