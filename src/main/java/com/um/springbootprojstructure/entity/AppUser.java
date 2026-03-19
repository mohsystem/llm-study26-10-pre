package com.um.springbootprojstructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public, URL-safe, stable reference for client-facing URLs/routes.
     * UUID string is URL-safe (no spaces) and non-guessable enough for many cases.
     */
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String publicRef;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    // Profile fields (example)
    @Column(length = 80)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(length = 300)
    private String avatarUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @PrePersist
    void ensurePublicRef() {
        if (this.publicRef == null || this.publicRef.isBlank()) {
            this.publicRef = UUID.randomUUID().toString();
        }
    }
}