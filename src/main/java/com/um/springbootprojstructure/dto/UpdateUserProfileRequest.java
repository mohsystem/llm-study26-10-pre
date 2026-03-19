package com.um.springbootprojstructure.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserProfileRequest {

    @Size(max = 80)
    private String displayName;

    @Size(max = 500)
    private String bio;

    @Size(max = 300)
    private String avatarUrl;
}