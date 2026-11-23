package com.lucas.pacioli_ledger.identity.infra.persistence;

import com.lucas.pacioli_ledger.identity.domain.User;
import com.lucas.pacioli_ledger.shared.domain.id.UserID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaUserRepositoryImplTest {

    @Mock
    private UserEntityRepository repository;

    @InjectMocks
    private JpaUserRepositoryImpl userRepository;

    @Test
    void shouldSaveUserAsEntity() {
        // Arrange
        User user = new User(
                "keycloak-sub",
                "Lucas",
                "lucas@example.com"
        );

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userRepository.save(user);

        // Assert
        verify(repository).save(captor.capture());

        UserEntity entity = captor.getValue();

        assertEquals(user.getId().id(), entity.getId());
        assertEquals(user.getIdentitySubject(), entity.getIdentitySubject());
        assertEquals(user.getName(), entity.getName());
        assertEquals(user.getEmail(), entity.getEmail());
        assertEquals(user.getCreatedAt(), entity.getCreatedAt());
    }

    @Test
    void shouldReturnUserIdWhenIdentitySubjectExists() {
        // Arrange
        String identitySubject = "keycloak-sub";
        UUID expectedId = UUID.randomUUID();

        when(repository.findIdByIdentitySubject(identitySubject))
                .thenReturn(Optional.of(expectedId));

        // Act
        Optional<UserID> result =
                userRepository.findIdByIdentitySubject(identitySubject);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedId, result.get().id());

        verify(repository).findIdByIdentitySubject(identitySubject);
    }

    @Test
    void shouldReturnEmptyWhenIdentitySubjectDoesNotExist() {
        // Arrange
        String identitySubject = "unknown-sub";

        when(repository.findIdByIdentitySubject(identitySubject))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserID> result =
                userRepository.findIdByIdentitySubject(identitySubject);

        // Assert
        assertTrue(result.isEmpty());

        verify(repository).findIdByIdentitySubject(identitySubject);
    }
}
