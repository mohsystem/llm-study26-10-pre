package com.um.springbootprojstructure.config;

import com.um.springbootprojstructure.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-ttl-seconds}") long accessTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public String generateAccessToken(String username, Set<Role> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("roles", roles.stream().map(Enum::name).toList())
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public Set<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (!(rolesObj instanceof Iterable<?> iterable)) {
            return Set.of();
        }

        return ((Iterable<?>) iterable)
                .iterator()
                .hasNext()
                ? ((java.util.List<?>) rolesObj).stream()
                .map(Object::toString)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet())
                : Set.of();
    }
}