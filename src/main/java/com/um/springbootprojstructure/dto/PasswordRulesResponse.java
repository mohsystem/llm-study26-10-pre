package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PasswordRulesResponse {
    private int minLength;
    private int maxLength;

    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;

    private String specialChars;

    private Instant updatedAt;
    private String updatedBy;
}