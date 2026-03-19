package com.um.springbootprojstructure.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserMergeRequest {

    @NotBlank
    private String sourcePublicRef;

    @NotBlank
    private String targetPublicRef;
}