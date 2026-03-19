package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DirectoryValidationResponse {

    private boolean matched;     // exactly one match
    private boolean ambiguous;   // multiple matches
    private int matchCount;

    /**
     * When matched==true (exactly one), include normalized attributes.
     */
    private Map<String, Object> entry;

    /**
     * When ambiguous==true, include a small list of candidates.
     */
    private List<Map<String, Object>> candidates;

    private String message;
}