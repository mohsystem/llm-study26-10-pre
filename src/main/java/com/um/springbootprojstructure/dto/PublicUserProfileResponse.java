package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicUserProfileResponse {
    private String publicRef;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
}