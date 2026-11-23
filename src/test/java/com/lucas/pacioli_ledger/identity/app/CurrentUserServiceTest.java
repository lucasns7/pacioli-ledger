package com.lucas.pacioli_ledger.identity.app;

import com.lucas.pacioli_ledger.identity.domain.User;
import com.lucas.pacioli_ledger.identity.domain.UserRepository;
import com.lucas.pacioli_ledger.shared.domain.id.UserID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnExistingUserId() {
        // Arrange
        Jwt jwt = createJwt();

        setAuthentication(jwt);

        UserID expectedId = UserID.generate();

        when(userRepository.findIdByIdentitySubject("keycloak-sub"))
                .thenReturn(Optional.of(expectedId));

        // Act
        UserID result = currentUserService.getCurrentUserID();

        // Assert
        assertEquals(expectedId, result);

        verify(userRepository).findIdByIdentitySubject("keycloak-sub");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldCreateUserWhenUserDoesNotExist() {
        // Arrange
        Jwt jwt = createJwt();

        setAuthentication(jwt);

        when(userRepository.findIdByIdentitySubject("keycloak-sub"))
                .thenReturn(Optional.empty());

        // Act
        UserID result = currentUserService.getCurrentUserID();

        // Assert
        assertNotNull(result);

        verify(userRepository).findIdByIdentitySubject("keycloak-sub");

        verify(userRepository).save(argThat(user ->
                user.getIdentitySubject().equals("keycloak-sub")
                        && user.getName().equals("Lucas")
                        && user.getEmail().equals("lucas@example.com")
        ));
    }

    @Test
    void shouldThrowWhenAuthenticationIsMissing() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                () -> currentUserService.getCurrentUserID()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowWhenPrincipalIsNotJwt() {
        // Arrange
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymous");

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // Act & Assert
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                () -> currentUserService.getCurrentUserID()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowWhenJwtSubjectIsMissing() {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("name", "Lucas")
                .claim("email", "lucas@example.com")
                .build();

        setAuthentication(jwt);

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> currentUserService.getCurrentUserID()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowWhenRequiredClaimsAreMissing() {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("keycloak-sub")
                .build();

        setAuthentication(jwt);

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> currentUserService.getCurrentUserID()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    private Jwt createJwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("keycloak-sub")
                .claim("name", "Lucas")
                .claim("email", "lucas@example.com")
                .build();
    }

    private void setAuthentication(Jwt jwt) {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}