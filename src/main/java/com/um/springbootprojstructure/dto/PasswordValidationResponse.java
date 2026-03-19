package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PasswordValidationResponse {
    private boolean accepted;
    private List<String> violations;
}