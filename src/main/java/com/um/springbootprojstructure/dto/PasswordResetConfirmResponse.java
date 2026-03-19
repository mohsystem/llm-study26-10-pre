package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordResetConfirmResponse {
    private boolean reset;
    private PasswordValidationResponse passwordValidation;
    private String message;
}