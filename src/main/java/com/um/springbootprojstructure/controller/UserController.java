package com.um.springbootprojstructure.controller;

import com.um.springbootprojstructure.dto.PublicUserProfileResponse;
import com.um.springbootprojstructure.dto.UpdateUserProfileRequest;
import com.um.springbootprojstructure.dto.UserResponse;
import com.um.springbootprojstructure.mapper.UserMapper;
import com.um.springbootprojstructure.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        var user = userService.getByUsername(authentication.getName());
        return UserMapper.toResponse(user);
    }

    @GetMapping("/{publicRef}")
    public PublicUserProfileResponse getPublicProfile(@PathVariable String publicRef) {
        var user = userService.getByPublicRef(publicRef);
        return UserMapper.toPublicProfile(user);
    }

    @PutMapping("/{publicRef}")
    public PublicUserProfileResponse updateProfile(
            @PathVariable String publicRef,
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication
    ) {
        var updated = userService.updateUserProfile(
                publicRef,
                request.getDisplayName(),
                request.getBio(),
                request.getAvatarUrl(),
                authentication
        );
        return UserMapper.toPublicProfile(updated);
    }
}