package com.um.springbootprojstructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "password_rules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PasswordRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic rules
    @Column(nullable = false)
    private int minLength;

    @Column(nullable = false)
    private int maxLength;

    @Column(nullable = false)
    private boolean requireUppercase;

    @Column(nullable = false)
    private boolean requireLowercase;

    @Column(nullable = false)
    private boolean requireDigit;

    @Column(nullable = false)
    private boolean requireSpecial;

    /**
     * Characters considered "special" when requireSpecial = true.
     * You can widen this set as desired.
     */
    @Column(nullable = false, length = 100)
    private String specialChars;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false, length = 80)
    private String updatedBy;
}