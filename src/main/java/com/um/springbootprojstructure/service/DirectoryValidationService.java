package com.um.springbootprojstructure.service;

import com.um.springbootprojstructure.dto.DirectoryValidationRequest;
import com.um.springbootprojstructure.dto.DirectoryValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.stereotype.Service;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DirectoryValidationService {

    private final LdapTemplate ldapTemplate;

    @Value("${app.ldap.enabled:true}")
    private boolean ldapEnabled;

    @Value("${app.ldap.user-search-base:}")
    private String userSearchBase;

    // Which attributes to return (adjust to your directory schema)
    private static final List<String> RETURN_ATTRS = List.of(
            "dn",
            "uid",
            "sAMAccountName",
            "cn",
            "givenName",
            "sn",
            "mail",
            "employeeID"
    );

    public DirectoryValidationResponse validate(DirectoryValidationRequest request) {
        if (!ldapEnabled) {
            return DirectoryValidationResponse.builder()
                    .matched(false)
                    .ambiguous(false)
                    .matchCount(0)
                    .message("LDAP validation is disabled by configuration (app.ldap.enabled=false)")
                    .build();
        }

        String username = trimToNull(request.getUsername());
        String email = trimToNull(request.getEmail());
        String employeeId = trimToNull(request.getEmployeeId());

        if (username == null && email == null && employeeId == null) {
            throw new IllegalArgumentException("Provide at least one identity attribute: username, email, employeeId");
        }

        // Build filter:
        // (&(objectClass=person)(|(uid=jdoe)(sAMAccountName=jdoe)(mail=jdoe@x.com)(employeeID=123)))
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "person"));

        OrFilter or = new OrFilter();
        if (username != null) {
            or.or(new EqualsFilter("uid", username));
            or.or(new EqualsFilter("sAMAccountName", username)); // AD
        }
        if (email != null) {
            or.or(new EqualsFilter("mail", email));
        }
        if (employeeId != null) {
            or.or(new EqualsFilter("employeeID", employeeId));
        }
        filter.and(or);

        List<Map<String, Object>> matches = ldapTemplate.search(
                userSearchBase == null ? "" : userSearchBase,
                filter.encode(),
                (AttributesMapper<Map<String, Object>>) attrs -> toMap(attrs)
        );

        if (matches.isEmpty()) {
            return DirectoryValidationResponse.builder()
                    .matched(false)
                    .ambiguous(false)
                    .matchCount(0)
                    .message("No directory entry matched the provided identity attributes.")
                    .build();
        }

        if (matches.size() == 1) {
            return DirectoryValidationResponse.builder()
                    .matched(true)
                    .ambiguous(false)
                    .matchCount(1)
                    .entry(matches.getFirst())
                    .message("Directory match found.")
                    .build();
        }

        // Multiple matches => ambiguous; return limited candidate list
        List<Map<String, Object>> candidates = matches.stream()
                .limit(10)
                .map(this::minimizeCandidate)
                .toList();

        return DirectoryValidationResponse.builder()
                .matched(false)
                .ambiguous(true)
                .matchCount(matches.size())
                .candidates(candidates)
                .message("Multiple directory entries matched. Provide more attributes to disambiguate.")
                .build();
    }

    private Map<String, Object> minimizeCandidate(Map<String, Object> full) {
        Map<String, Object> m = new LinkedHashMap<>();
        copyIfPresent(full, m, "dn");
        copyIfPresent(full, m, "uid");
        copyIfPresent(full, m, "sAMAccountName");
        copyIfPresent(full, m, "cn");
        copyIfPresent(full, m, "mail");
        copyIfPresent(full, m, "employeeID");
        return m;
    }

    private static void copyIfPresent(Map<String, Object> src, Map<String, Object> dst, String key) {
        if (src.containsKey(key)) dst.put(key, src.get(key));
    }

    private Map<String, Object> toMap(Attributes attrs) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();

        // Attempt to include DN if available from "distinguishedName" or similar.
        // Many servers don't return "dn" as an attribute; so we just map what exists.
        for (String attr : RETURN_ATTRS) {
            Attribute a = attrs.get(attr);
            if (a == null) continue;

            if (a.size() == 1) {
                map.put(attr, a.get());
            } else {
                List<Object> values = new ArrayList<>();
                NamingEnumeration<?> all = a.getAll();
                while (all.hasMore()) values.add(all.next());
                map.put(attr, values);
            }
        }

        // Also include whatever else the directory returned (optional).
        // Comment out if you want strict attribute whitelist.
        NamingEnumeration<? extends Attribute> all = attrs.getAll();
        while (all.hasMore()) {
            Attribute a = all.next();
            String id = a.getID();
            if (map.containsKey(id)) continue;

            if (a.size() == 1) map.put(id, a.get());
            else {
                List<Object> values = new ArrayList<>();
                NamingEnumeration<?> vals = a.getAll();
                while (vals.hasMore()) values.add(vals.next());
                map.put(id, values);
            }
        }

        return map;
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}