package com.lucas.pacioli_ledger.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String API_CLIENT_ID = "pacioli-api";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtGrantedAuthoritiesConverter scopeConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        authorities.addAll(extractScopeAuthorities(jwt));
        authorities.addAll(extractClientRoles(jwt));

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractScopeAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = scopeConverter.convert(jwt);

        return authorities != null ? authorities : Set.of();
    }

    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        var resourceAccessClaim = jwt.getClaims().get(RESOURCE_ACCESS_CLAIM);

        if (!(resourceAccessClaim instanceof Map<?, ?> resourceAccess)) {
            return Set.of();
        }

        var clientAccessClaim = resourceAccess.get(API_CLIENT_ID);

        if (!(clientAccessClaim instanceof Map<?, ?> clientAccess)) {
            return Set.of();
        }

        var rolesClaim = clientAccess.get(ROLES_CLAIM);

        if (!(rolesClaim instanceof Collection<?> roles)) {
            return Set.of();
        }

        Collection<GrantedAuthority> authorities = new LinkedHashSet<>();

        for (var role : roles) {
            if (role instanceof String roleName && !roleName.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + roleName));
            }
        }

        return authorities;
    }
}
