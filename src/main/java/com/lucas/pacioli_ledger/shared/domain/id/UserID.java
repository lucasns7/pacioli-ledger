package com.lucas.pacioli_ledger.shared.domain.id;

import java.util.Objects;
import java.util.UUID;

public record UserID(UUID id) {

    public UserID {
        Objects.requireNonNull(id, "UserID cannot be null");
    }

    public static UserID generate(){
        return new UserID(UUID.randomUUID());
    }
}
