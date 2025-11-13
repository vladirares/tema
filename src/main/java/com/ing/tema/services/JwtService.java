package com.ing.tema.services;

import com.ing.tema.security.JwtConfigProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtEncoder jwtEncoder;
    private final JwtConfigProperties properties;

    public JwtService(JwtEncoder jwtEncoder, JwtConfigProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        long expiry = properties.getExpirationSeconds();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // e.g. ROLE_ADMIN
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("store-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(authentication.getName())
                .claim("roles", roles)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
        log.info("Generated JWT token for user {}", authentication.getName());

        return jwt.getTokenValue();
    }

    public long getExpirationSeconds() {
        return properties.getExpirationSeconds();
    }
}
