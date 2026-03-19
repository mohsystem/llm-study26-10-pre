package com.um.springbootprojstructure.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class XmlImportSummaryResponse {
    private int totalRecords;
    private int importedCount;
    private int skippedCount;
    private int rejectedCount;

    private List<ImportedRecord> imported;
    private List<SkippedRecord> skipped;
    private List<RejectedRecord> rejected;

    @Getter
    @Builder
    public static class ImportedRecord {
        private String legacyId;
        private String username;
        private String publicRef;
    }

    @Getter
    @Builder
    public static class SkippedRecord {
        private String legacyId;
        private String username;
        private String reason; // e.g. "duplicate username", "duplicate email"
    }

    @Getter
    @Builder
    public static class RejectedRecord {
        private String legacyId;
        private String username;
        private String reason; // e.g. "missing email", "invalid xml", "invalid email"
    }
}