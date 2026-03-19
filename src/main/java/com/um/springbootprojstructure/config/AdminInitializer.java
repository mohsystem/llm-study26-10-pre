package com.um.springbootprojstructure.config;

import com.um.springbootprojstructure.entity.AppUser;
import com.um.springbootprojstructure.entity.Role;
import com.um.springbootprojstructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.email:admin@example.com}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (!enabled) return;

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        AppUser admin = AppUser.builder()
                .publicRef(UUID.randomUUID().toString())
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .roles(Set.of(Role.ADMIN, Role.USER))
                .enabled(true)
                .build();

        userRepository.save(admin);
    }
}