package com.um.springbootprojstructure.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DirectoryValidationRequest {

    /**
     * Provide at least one of: username, email, employeeId.
     */
    @Size(max = 120)
    private String username;

    @Size(max = 254)
    private String email;

    @Size(max = 64)
    private String employeeId;
}