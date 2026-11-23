package com.lucas.pacioli_ledger.identity.infra.persistence;

import com.lucas.pacioli_ledger.identity.domain.User;
import com.lucas.pacioli_ledger.shared.domain.id.UserID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Lombok;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_identity_subject",
                columnNames = "identity_subject"
        )
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "identity_subject", nullable = false)
    private String identitySubject;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 150, nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private UserEntity(
            UUID id,
            String identitySubject,
            String name,
            String email,
            Instant createdAt
    ) {
        this.id = id;
        this.identitySubject = identitySubject;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public static UserEntity from(User user){
        return new UserEntity(
                user.getId().id(),
                user.getIdentitySubject(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
