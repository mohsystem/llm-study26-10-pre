package com.um.springbootprojstructure.dto;

import com.um.springbootprojstructure.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class RoleAssignmentResponse {
    private Long userId;
    private Set<Role> roles;
}