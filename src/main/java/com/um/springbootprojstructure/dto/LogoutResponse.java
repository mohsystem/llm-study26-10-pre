package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutResponse {
    private boolean success;
    private String message;
}