package com.lucas.pacioli_ledger.shared.domain.id;

import java.util.Objects;
import java.util.UUID;

public record AccountID(UUID id) {

    public AccountID{
        Objects.requireNonNull(id, "AccountID cannot be null");
    }

    public static AccountID generate(){
        return new AccountID(UUID.randomUUID());
    }
}
