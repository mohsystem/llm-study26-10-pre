package com.um.springbootprojstructure.service;

import com.um.springbootprojstructure.config.JwtService;
import com.um.springbootprojstructure.dto.AuthResponse;
import com.um.springbootprojstructure.entity.AppUser;
import com.um.springbootprojstructure.entity.RefreshToken;
import com.um.springbootprojstructure.repository.RefreshTokenRepository;
import com.um.springbootprojstructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-ttl-seconds}")
    private long refreshTtlSeconds;

    @Transactional
    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRoles());

        // Create refresh token (rotate on every login)
        String refreshTokenRaw = UUID.randomUUID().toString() + UUID.randomUUID(); // longer random string
        String refreshHash = sha256Hex(refreshTokenRaw);

        RefreshToken refresh = RefreshToken.builder()
                .tokenHash(refreshHash)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refresh);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenRaw)
                .tokenType("Bearer")
                .build();
    }

    /**
     * POST /api/auth/refresh
     * Accepts refresh token, validates it (not revoked, not expired), then issues a new access token.
     * Optionally rotates refresh token (implemented below).
     */
    @Transactional
    public AuthResponse refresh(String refreshTokenRaw) {
        String hash = sha256Hex(refreshTokenRaw);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (existing.isRevoked()) {
            throw new IllegalArgumentException("Refresh token revoked");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        AppUser user = existing.getUser(); // ok within transaction (LAZY)

        // Issue new access token
        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getRoles());

        // Rotate refresh token: revoke old, create new
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        String newRefreshRaw = UUID.randomUUID().toString() + UUID.randomUUID();
        RefreshToken rotated = RefreshToken.builder()
                .tokenHash(sha256Hex(newRefreshRaw))
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rotated);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshRaw)
                .tokenType("Bearer")
                .build();
    }

    public Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash refresh token", e);
        }
    }

    @Transactional
    public boolean logout(String refreshTokenRaw) {
        String hash = sha256Hex(refreshTokenRaw);
        long deleted = refreshTokenRepository.deleteByTokenHash(hash);
        // If deleted==0, token was already invalid/expired/revoked; we still treat logout as idempotent success.
        return true;
    }
}