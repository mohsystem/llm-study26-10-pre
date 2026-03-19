package com.um.springbootprojstructure.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.um.springbootprojstructure.dto.XmlImportSummaryResponse;
import com.um.springbootprojstructure.dto.legacy.LegacyUserXml;
import com.um.springbootprojstructure.dto.legacy.LegacyUsersXml;
import com.um.springbootprojstructure.entity.AppUser;
import com.um.springbootprojstructure.entity.Role;
import com.um.springbootprojstructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdminUserImportService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Simple email validation (you can replace with Apache Commons Validator if desired)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Parses and imports legacy users from XML.
     * - imported: created
     * - skipped: duplicates (username/email)
     * - rejected: invalid record (missing username/email, invalid email, etc.)
     */
    @Transactional
    public XmlImportSummaryResponse importUsersFromXml(InputStream xmlStream) {
        XmlMapper xmlMapper = XmlMapper.builder().build();

        LegacyUsersXml legacy;
        try {
            legacy = xmlMapper.readValue(xmlStream, LegacyUsersXml.class);
        } catch (Exception e) {
            return XmlImportSummaryResponse.builder()
                    .totalRecords(0)
                    .importedCount(0)
                    .skippedCount(0)
                    .rejectedCount(1)
                    .imported(List.of())
                    .skipped(List.of())
                    .rejected(List.of(
                            XmlImportSummaryResponse.RejectedRecord.builder()
                                    .legacyId(null)
                                    .username(null)
                                    .reason("Invalid XML: " + e.getMessage())
                                    .build()
                    ))
                    .build();
        }

        List<LegacyUserXml> records = legacy.getUser() == null ? List.of() : legacy.getUser();

        List<XmlImportSummaryResponse.ImportedRecord> imported = new ArrayList<>();
        List<XmlImportSummaryResponse.SkippedRecord> skipped = new ArrayList<>();
        List<XmlImportSummaryResponse.RejectedRecord> rejected = new ArrayList<>();

        for (LegacyUserXml r : records) {
            String legacyId = safeTrim(r.getId());
            String username = safeTrim(r.getUsername());
            String email = safeTrim(r.getEmail());
            String displayName = safeTrim(r.getDisplayName());
            boolean enabled = r.getEnabled() == null || r.getEnabled(); // default true

            // Basic validation
            if (isBlank(username)) {
                rejected.add(XmlImportSummaryResponse.RejectedRecord.builder()
                        .legacyId(legacyId)
                        .username(null)
                        .reason("Missing username")
                        .build());
                continue;
            }
            if (isBlank(email)) {
                rejected.add(XmlImportSummaryResponse.RejectedRecord.builder()
                        .legacyId(legacyId)
                        .username(username)
                        .reason("Missing email")
                        .build());
                continue;
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                rejected.add(XmlImportSummaryResponse.RejectedRecord.builder()
                        .legacyId(legacyId)
                        .username(username)
                        .reason("Invalid email")
                        .build());
                continue;
            }

            // Skip duplicates
            if (userRepository.existsByUsername(username)) {
                skipped.add(XmlImportSummaryResponse.SkippedRecord.builder()
                        .legacyId(legacyId)
                        .username(username)
                        .reason("Duplicate username")
                        .build());
                continue;
            }
            if (userRepository.existsByEmail(email)) {
                skipped.add(XmlImportSummaryResponse.SkippedRecord.builder()
                        .legacyId(legacyId)
                        .username(username)
                        .reason("Duplicate email")
                        .build());
                continue;
            }

            // Create user
            // Since legacy export typically doesn't include password hashes we can trust,
            // assign a random password to force reset later (in a real system you'd add a reset flow).
            String randomPassword = UUID.randomUUID().toString();

            AppUser user = AppUser.builder()
                    // publicRef generated by @PrePersist if you implemented it;
                    // setting explicitly is also fine:
                    .publicRef(UUID.randomUUID().toString())
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(randomPassword))
                    .displayName(displayName)
                    .enabled(enabled)
                    .roles(Set.of(Role.USER))
                    .build();

            AppUser saved = userRepository.save(user);

            imported.add(XmlImportSummaryResponse.ImportedRecord.builder()
                    .legacyId(legacyId)
                    .username(saved.getUsername())
                    .publicRef(saved.getPublicRef())
                    .build());
        }

        return XmlImportSummaryResponse.builder()
                .totalRecords(records.size())
                .importedCount(imported.size())
                .skippedCount(skipped.size())
                .rejectedCount(rejected.size())
                .imported(imported)
                .skipped(skipped)
                .rejected(rejected)
                .build();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}