package com.um.springbootprojstructure.repository;

import com.um.springbootprojstructure.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    long deleteByTokenHash(String tokenHash);

    long deleteByExpiresAtBefore(Instant now);
    long deleteByUser_Id(Long userId);
}