package com.um.springbootprojstructure.service;

import com.um.springbootprojstructure.dto.PasswordRulesRequest;
import com.um.springbootprojstructure.dto.PasswordRulesResponse;
import com.um.springbootprojstructure.entity.PasswordRules;
import com.um.springbootprojstructure.repository.PasswordRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.um.springbootprojstructure.dto.PasswordValidationResponse;
import com.um.springbootprojstructure.entity.PasswordRules;
import com.um.springbootprojstructure.repository.PasswordRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PasswordRulesService {

    private final PasswordRulesRepository passwordRulesRepository;

    /**
     * Returns active rules. If none exist yet, it lazily creates a default set.
     */
    @Transactional
    public PasswordRules getActiveRules() {
        return passwordRulesRepository.findLatest()
                .orElseGet(() -> passwordRulesRepository.save(defaultRules("system")));
    }

    @Transactional
    public PasswordRulesResponse getActiveRulesResponse() {
        return toResponse(getActiveRules());
    }

    @Transactional
    public PasswordRulesResponse updateRules(PasswordRulesRequest request, String updatedBy) {
        if (request.getMinLength() > request.getMaxLength()) {
            throw new IllegalArgumentException("minLength must be <= maxLength");
        }
        if (request.isRequireSpecial() && (request.getSpecialChars() == null || request.getSpecialChars().isBlank())) {
            throw new IllegalArgumentException("specialChars must be provided when requireSpecial=true");
        }

        PasswordRules rules = PasswordRules.builder()
                .minLength(request.getMinLength())
                .maxLength(request.getMaxLength())
                .requireUppercase(request.isRequireUppercase())
                .requireLowercase(request.isRequireLowercase())
                .requireDigit(request.isRequireDigit())
                .requireSpecial(request.isRequireSpecial())
                .specialChars(request.getSpecialChars())
                .updatedAt(Instant.now())
                .updatedBy(updatedBy)
                .build();

        passwordRulesRepository.save(rules);
        return toResponse(rules);
    }



    private PasswordRulesResponse toResponse(PasswordRules r) {
        return PasswordRulesResponse.builder()
                .minLength(r.getMinLength())
                .maxLength(r.getMaxLength())
                .requireUppercase(r.isRequireUppercase())
                .requireLowercase(r.isRequireLowercase())
                .requireDigit(r.isRequireDigit())
                .requireSpecial(r.isRequireSpecial())
                .specialChars(r.getSpecialChars())
                .updatedAt(r.getUpdatedAt())
                .updatedBy(r.getUpdatedBy())
                .build();
    }
    @Transactional(readOnly = true)
    public PasswordValidationResponse validatePassword(String rawPassword) {
        PasswordRules rules = passwordRulesRepository.findLatest()
                .orElseGet(() -> defaultRules("system")); // default if not configured yet

        List<String> violations = new ArrayList<>();

        if (rawPassword == null) {
            violations.add("Password is required");
            return PasswordValidationResponse.builder().accepted(false).violations(violations).build();
        }

        int len = rawPassword.length();
        if (len < rules.getMinLength() || len > rules.getMaxLength()) {
            violations.add("Password length must be between " + rules.getMinLength() + " and " + rules.getMaxLength());
        }
        if (rules.isRequireUppercase() && rawPassword.chars().noneMatch(Character::isUpperCase)) {
            violations.add("Password must contain an uppercase letter");
        }
        if (rules.isRequireLowercase() && rawPassword.chars().noneMatch(Character::isLowerCase)) {
            violations.add("Password must contain a lowercase letter");
        }
        if (rules.isRequireDigit() && rawPassword.chars().noneMatch(Character::isDigit)) {
            violations.add("Password must contain a digit");
        }
        if (rules.isRequireSpecial()) {
            String specials = rules.getSpecialChars() == null ? "" : rules.getSpecialChars();
            boolean hasSpecial = rawPassword.chars()
                    .mapToObj(c -> String.valueOf((char) c))
                    .anyMatch(specials::contains);
            if (!hasSpecial) {
                violations.add("Password must contain a special character from: " + specials);
            }
        }

        return PasswordValidationResponse.builder()
                .accepted(violations.isEmpty())
                .violations(violations)
                .build();
    }

    /**
     * Keep your existing defaultRules(...) method (must exist in the class).
     * If your previous defaultRules was private, keep it private here too.
     */
    private PasswordRules defaultRules(String updatedBy) {
        return PasswordRules.builder()
                .minLength(8)
                .maxLength(64)
                .requireUppercase(true)
                .requireLowercase(true)
                .requireDigit(true)
                .requireSpecial(false)
                .specialChars("!@#$%^&*()-_=+[]{};:,.?")
                .updatedAt(java.time.Instant.now())
                .updatedBy(updatedBy)
                .build();
    }
}