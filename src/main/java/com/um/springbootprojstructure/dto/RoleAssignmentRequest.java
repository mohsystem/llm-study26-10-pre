package com.um.springbootprojstructure.dto;

import com.um.springbootprojstructure.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class RoleAssignmentRequest {

    @NotEmpty
    private Set<Role> roles;
}