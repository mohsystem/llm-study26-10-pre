package com.um.springbootprojstructure.service;

import com.um.springbootprojstructure.dto.UserMergeResultResponse;
import com.um.springbootprojstructure.entity.AppUser;
import com.um.springbootprojstructure.entity.UserMergeAudit;
import com.um.springbootprojstructure.repository.RefreshTokenRepository;
import com.um.springbootprojstructure.repository.UserMergeAuditRepository;
import com.um.springbootprojstructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMergeAuditRepository userMergeAuditRepository;

    @Transactional
    public UserMergeResultResponse mergeUsers(String sourcePublicRef, String targetPublicRef, String performedBy) {
        if (sourcePublicRef.equals(targetPublicRef)) {
            throw new IllegalArgumentException("Source and target must be different users");
        }

        AppUser source = userRepository.findByPublicRef(sourcePublicRef)
                .orElseThrow(() -> new IllegalArgumentException("Source user not found"));

        AppUser target = userRepository.findByPublicRef(targetPublicRef)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        if (!source.isEnabled()) {
            throw new IllegalArgumentException("Source user is already disabled");
        }
        if (!target.isEnabled()) {
            throw new IllegalArgumentException("Target user is disabled; cannot merge into a disabled account");
        }

        // 1) Merge roles (union)
        var mergedRoles = new HashSet<>(target.getRoles());
        mergedRoles.addAll(source.getRoles());
        target.setRoles(mergedRoles);

        // 2) Merge profile fields: "fill missing on target from source"
        if (isBlank(target.getDisplayName()) && !isBlank(source.getDisplayName())) {
            target.setDisplayName(source.getDisplayName());
        }
        if (isBlank(target.getBio()) && !isBlank(source.getBio())) {
            target.setBio(source.getBio());
        }
        if (isBlank(target.getAvatarUrl()) && !isBlank(source.getAvatarUrl())) {
            target.setAvatarUrl(source.getAvatarUrl());
        }

        // NOTE: We intentionally DO NOT merge username/email/password automatically.
        // - username/email are unique and are identifiers for login/notifications
        // - choosing them should be explicit and/or validated by business rules

        // 3) Disable source account (so it can't be used anymore)
        source.setEnabled(false);

        // 4) Revoke refresh tokens for source (delete all refresh tokens)
        long revokedCount = refreshTokenRepository.deleteByUser_Id(source.getId());
        boolean refreshTokensRevoked = revokedCount > 0;

        userRepository.save(target);
        userRepository.save(source);

        String summary = "Merged source user " + sourcePublicRef + " into target user " + targetPublicRef
                + ". Source disabled; roles unioned; profile fields filled on target where empty; "
                + "refresh tokens revoked=" + refreshTokensRevoked + ".";

        userMergeAuditRepository.save(
                UserMergeAudit.builder()
                        .sourcePublicRef(sourcePublicRef)
                        .targetPublicRef(targetPublicRef)
                        .performedBy(performedBy)
                        .performedAt(Instant.now())
                        .summary(summary)
                        .build()
        );

        return UserMergeResultResponse.builder()
                .sourcePublicRef(sourcePublicRef)
                .targetPublicRef(targetPublicRef)
                .sourceDisabled(true)
                .refreshTokensRevokedForSource(refreshTokensRevoked)
                .targetRolesAfter(target.getRoles())
                .displayNameAfter(target.getDisplayName())
                .bioAfter(target.getBio())
                .avatarUrlAfter(target.getAvatarUrl())
                .summary(summary)
                .build();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}