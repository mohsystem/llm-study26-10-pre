package com.um.springbootprojstructure.dto;

import com.um.springbootprojstructure.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class RoleUpdateRequest {
    @NotNull
    private Set<Role> roles;
}