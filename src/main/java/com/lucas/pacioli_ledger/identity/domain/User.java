package com.lucas.pacioli_ledger.identity.domain;

import com.lucas.pacioli_ledger.shared.domain.id.UserID;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class User {

    private UserID id;
    private String identitySubject;
    private String name;
    private String email;
    private Instant createdAt;

    public User(String identitySubject, String name, String email) {
        this.id = UserID.generate();

        this.identitySubject = Objects.requireNonNull(
                identitySubject, "Identity subject cannot be null"
        );

        this.name = name;
        this.email = email;
        this.createdAt = Instant.now();
    }
}
