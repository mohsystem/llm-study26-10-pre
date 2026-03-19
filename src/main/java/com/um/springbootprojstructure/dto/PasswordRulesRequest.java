package com.um.springbootprojstructure.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordRulesRequest {

    @Min(6)
    @Max(200)
    private int minLength;

    @Min(6)
    @Max(200)
    private int maxLength;

    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;

    @NotBlank
    private String specialChars;
}