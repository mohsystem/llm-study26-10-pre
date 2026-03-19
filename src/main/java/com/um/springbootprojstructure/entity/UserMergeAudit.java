package com.um.springbootprojstructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_merge_audit",
        indexes = {
                @Index(name = "idx_merge_source", columnList = "sourcePublicRef"),
                @Index(name = "idx_merge_target", columnList = "targetPublicRef")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserMergeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String sourcePublicRef;

    @Column(nullable = false, length = 36)
    private String targetPublicRef;

    @Column(nullable = false)
    private String performedBy; // admin username (principal)

    @Column(nullable = false)
    private Instant performedAt;

    @Column(nullable = false, length = 500)
    private String summary;
}