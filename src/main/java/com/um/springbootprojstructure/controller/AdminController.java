package com.um.springbootprojstructure.controller;

import com.um.springbootprojstructure.dto.RoleAssignmentRequest;
import com.um.springbootprojstructure.dto.RoleAssignmentResponse;
import com.um.springbootprojstructure.dto.RoleUpdateRequest;
import com.um.springbootprojstructure.dto.UserMergeRequest;
import com.um.springbootprojstructure.dto.UserMergeResultResponse;
import com.um.springbootprojstructure.dto.UserResponse;
import com.um.springbootprojstructure.mapper.UserMapper;
import com.um.springbootprojstructure.service.AdminUserService;
import com.um.springbootprojstructure.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.um.springbootprojstructure.dto.DirectoryValidationRequest;
import com.um.springbootprojstructure.dto.DirectoryValidationResponse;
import com.um.springbootprojstructure.service.DirectoryValidationService;
import jakarta.validation.Valid;
import java.util.List;
import com.um.springbootprojstructure.dto.XmlImportSummaryResponse;
import com.um.springbootprojstructure.service.AdminUserImportService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminUserImportService adminUserImportService;
    private final DirectoryValidationService directoryValidationService;

    private final UserService userService;
    private final AdminUserService adminUserService;

    @PostMapping("/users/validate-directory")
    public DirectoryValidationResponse validateDirectory(@Valid @RequestBody DirectoryValidationRequest request) {
        return directoryValidationService.validate(request);
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return userService.listUsers().stream().map(UserMapper::toResponse).toList();
    }

    // Existing endpoint (keep if you want)
    @PutMapping("/users/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        var updated = userService.updateRoles(id, request.getRoles());
        return UserMapper.toResponse(updated);
    }

    // NEW endpoint required by spec:
    @PutMapping("/users/{id}/role")
    public RoleAssignmentResponse updateRoleAssignment(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignmentRequest request
    ) {
        var updated = userService.updateRoles(id, request.getRoles());
        return RoleAssignmentResponse.builder()
                .userId(updated.getId())
                .roles(updated.getRoles())
                .build();
    }

    @PostMapping("/users/merge")
    public UserMergeResultResponse mergeUsers(
            @Valid @RequestBody UserMergeRequest request,
            Authentication authentication
    ) {
        return adminUserService.mergeUsers(
                request.getSourcePublicRef(),
                request.getTargetPublicRef(),
                authentication.getName()
        );
    }

    @PostMapping(value = "/users/import-xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public XmlImportSummaryResponse importXml(@RequestPart("file") @NotNull MultipartFile file) throws Exception {
        try (var in = file.getInputStream()) {
            return adminUserImportService.importUsersFromXml(in);
        }
    }
}