package com.um.springbootprojstructure.controller;

import com.um.springbootprojstructure.dto.PasswordRulesRequest;
import com.um.springbootprojstructure.dto.PasswordRulesResponse;
import com.um.springbootprojstructure.service.PasswordRulesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountConfigController {

    private final PasswordRulesService passwordRulesService;

    @GetMapping("/password-rules")
    public PasswordRulesResponse getPasswordRules() {
        return passwordRulesService.getActiveRulesResponse();
    }

    @PutMapping("/password-rules")
    public PasswordRulesResponse updatePasswordRules(
            @Valid @RequestBody PasswordRulesRequest request,
            Authentication authentication
    ) {
        return passwordRulesService.updateRules(request, authentication.getName());
    }
}