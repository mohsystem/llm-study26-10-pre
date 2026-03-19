package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordChangeResponse {
    private boolean changed;
    private PasswordValidationResponse passwordValidation;
    private String message;
}