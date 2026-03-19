package com.um.springbootprojstructure.dto;

import com.um.springbootprojstructure.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserMergeResultResponse {
    private String sourcePublicRef;
    private String targetPublicRef;

    private boolean sourceDisabled;
    private boolean refreshTokensRevokedForSource;

    private Set<Role> targetRolesAfter;

    private String displayNameAfter;
    private String bioAfter;
    private String avatarUrlAfter;

    private String summary;
}