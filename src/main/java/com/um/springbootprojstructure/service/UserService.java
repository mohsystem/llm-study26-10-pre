package com.um.springbootprojstructure.service;

import com.um.springbootprojstructure.entity.AppUser;
import com.um.springbootprojstructure.entity.Role;
import com.um.springbootprojstructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.um.springbootprojstructure.entity.AppUser;
import com.um.springbootprojstructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.um.springbootprojstructure.service.PasswordRulesService;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordRulesService passwordRulesService;

    @Transactional
    public AppUser registerUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // NEW: enforce password rules
        passwordRulesService.validatePasswordOrThrow(rawPassword);

        AppUser user = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    public AppUser getByPublicRef(String publicRef) {
        return userRepository.findByPublicRef(publicRef)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<AppUser> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public AppUser updateRoles(Long userId, Set<Role> roles) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoles(roles);
        return userRepository.save(user);
    }
    @Transactional
    public AppUser updateUserProfile(String publicRef, String displayName, String bio, String avatarUrl, Authentication authentication) {
        AppUser user = userRepository.findByPublicRef(publicRef)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = authentication.getName().equals(user.getUsername());

        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed to update this profile");
        }

        // Only allowed fields updated (do NOT allow username/email/password/roles here)
        user.setDisplayName(displayName);
        user.setBio(bio);
        user.setAvatarUrl(avatarUrl);

        return userRepository.save(user);
    }
}