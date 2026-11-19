package com.lucas.pacioli_ledger.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthenticationConverterTest {

    private KeycloakJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KeycloakJwtAuthenticationConverter();
    }

    @Test
    void shouldConvertClientRolesAndScopes() {
        Jwt jwt = createJwt(
                Map.of(
                        "resource_access",
                        Map.of(
                                "pacioli-api",
                                Map.of("roles", Set.of("USER", "ADMIN"))
                        ),
                        "scope",
                        "profile email"
                )
        );

        JwtAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authorityNames(authentication.getAuthorities()))
                .containsExactlyInAnyOrder(
                        "ROLE_USER",
                        "ROLE_ADMIN",
                        "SCOPE_profile",
                        "SCOPE_email"
                );

        assertThat(authentication.getName()).isEqualTo("user-123");
    }

    @Test
    void shouldIgnoreRolesFromOtherClients() {
        Jwt jwt = createJwt(
                Map.of(
                        "resource_access",
                        Map.of(
                                "account",
                                Map.of("roles", Set.of("manage-account")),
                                "pacioli-api",
                                Map.of("roles", Set.of("USER"))
                        )
                )
        );

        JwtAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authorityNames(authentication.getAuthorities()))
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenClaimsAreMissing() {
        Jwt jwt = createJwt(Map.of());

        JwtAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getAuthorities()).isEmpty();
    }

    @Test
    void shouldIgnoreMalformedResourceAccessClaim() {
        Jwt jwt = createJwt(Map.of("resource_access", "invalid"));

        JwtAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getAuthorities()).isEmpty();
    }

    @Test
    void shouldIgnoreInvalidRoleValues() {
        Jwt jwt = createJwt(
                Map.of(
                        "resource_access",
                        Map.of(
                                "pacioli-api",
                                Map.of(
                                        "roles",
                                        java.util.Arrays.asList("USER", "", 123, null)
                                )
                        )
                )
        );

        JwtAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authorityNames(authentication.getAuthorities()))
                .containsExactly("ROLE_USER");
    }

    private Jwt createJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("user-123")
                .claim("iss", "http://localhost:8081/realms/pacioli")
                .claims(existingClaims -> existingClaims.putAll(claims))
                .build();
    }

    private Set<String> authorityNames(Collection<GrantedAuthority> authorities) {
        return authorities.stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
    }
}