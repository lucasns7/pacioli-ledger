
package com.lucas.pacioli_ledger.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = SecurityConfigTest.TestConfig.class)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtDecoder jwtDecoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(jwtDecoder);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ---------------------------------------------------------
    // Regras básicas de autenticação e autorização
    // ---------------------------------------------------------

    @Test
    void shouldAllowPublicEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectPrivateEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/private/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowPrivateEndpointWithValidAuthentication() throws Exception {
        mockMvc.perform(
                get("/api/private/ping")
                        .with(SecurityMockMvcRequestPostProcessors.jwt())
        ).andExpect(status().isOk());
    }

    @Test
    void shouldAllowEndpointWhenUserHasRequiredRole() throws Exception {
        mockMvc.perform(
                get("/api/private/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(status().isOk());
    }

    @Test
    void shouldRejectEndpointWhenUserDoesNotHaveRequiredRole() throws Exception {
        mockMvc.perform(
                get("/api/private/user")
                        .with(SecurityMockMvcRequestPostProcessors.jwt())
        ).andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // Integração do Bearer Token com o JwtAuthenticationConverter
    // ---------------------------------------------------------

    @Test
    void shouldAllowUserEndpointWhenTokenHasUserRole() throws Exception {
        performWithToken(
                "user-token",
                "/api/private/user",
                Map.of("pacioli-api", Map.of("roles", List.of("USER"))),
                null
        ).andExpect(status().isOk());
    }

    @Test
    void shouldRejectUserEndpointWhenTokenHasOnlyAdminRole() throws Exception {
        performWithToken(
                "admin-token",
                "/api/private/user",
                Map.of("pacioli-api", Map.of("roles", List.of("ADMIN"))),
                null
        ).andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminEndpointWhenTokenHasAdminRole() throws Exception {
        performWithToken(
                "admin-access-token",
                "/api/private/admin",
                Map.of("pacioli-api", Map.of("roles", List.of("ADMIN"))),
                null
        ).andExpect(status().isOk());
    }

    @Test
    void shouldIgnoreRolesFromAnotherClient() throws Exception {
        performWithToken(
                "another-client-token",
                "/api/private/user",
                Map.of("another-client", Map.of("roles", List.of("USER"))),
                null
        ).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectTokenWithoutResourceAccessClaim() throws Exception {
        performWithToken(
                "no-resource-access-token",
                "/api/private/user",
                null,
                null
        ).andExpect(status().isForbidden());
    }

    @Test
    void shouldNotTreatScopesAsApplicationRoles() throws Exception {
        performWithToken(
                "scope-only-token",
                "/api/private/user",
                null,
                "profile email pacioli-api-access"
        ).andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private ResultActions performWithToken(
            String token,
            String endpoint,
            Map<String, Object> resourceAccess,
            String scope
    ) throws Exception {

        Jwt jwt = createJwt(token, resourceAccess, scope);

        when(jwtDecoder.decode(token)).thenReturn(jwt);

        return mockMvc.perform(
                get(endpoint)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );
    }

    private Jwt createJwt(
            String token,
            Map<String, Object> resourceAccess,
            String scope
    ) {
        Instant now = Instant.now();

        Jwt.Builder builder = Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .subject("test-user-subject")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300));

        if (resourceAccess != null) {
            builder.claim("resource_access", resourceAccess);
        }

        if (scope != null) {
            builder.claim("scope", scope);
        }

        return builder.build();
    }

    // ---------------------------------------------------------
    // Configuração de teste
    // ---------------------------------------------------------

    @Configuration
    @EnableWebMvc
    @Import({
            SecurityConfig.class,
            KeycloakJwtAuthenticationConverter.class,
            SecurityExceptionHandler.class
    })
    static class TestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    // ---------------------------------------------------------
    // Endpoints utilizados exclusivamente pelos testes
    // ---------------------------------------------------------

    @RestController
    static class TestController {

        @GetMapping("/api/public/ping")
        String publicEndpoint() {
            return "Public endpoint";
        }

        @GetMapping("/api/private/ping")
        String privateEndpoint() {
            return "Private endpoint";
        }

        @GetMapping("/api/private/user")
        @PreAuthorize("hasRole('USER')")
        String userEndpoint() {
            return "User endpoint";
        }

        @GetMapping("/api/private/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String adminEndpoint() {
            return "Admin endpoint";
        }
    }
}
