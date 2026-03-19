package com.um.springbootprojstructure.mapper;

import com.um.springbootprojstructure.dto.PublicUserProfileResponse;
import com.um.springbootprojstructure.dto.UserResponse;
import com.um.springbootprojstructure.entity.AppUser;

public class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(AppUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .publicRef(user.getPublicRef()) // <-- add
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .build();
    }

    public static PublicUserProfileResponse toPublicProfile(AppUser user) {
        return PublicUserProfileResponse.builder()
                .publicRef(user.getPublicRef())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}