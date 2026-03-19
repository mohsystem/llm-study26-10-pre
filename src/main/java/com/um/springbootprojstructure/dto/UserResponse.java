package com.um.springbootprojstructure.dto;

import com.um.springbootprojstructure.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String publicRef;     // <-- add
    private String username;
    private String email;
    private Set<Role> roles;
    private boolean enabled;
}