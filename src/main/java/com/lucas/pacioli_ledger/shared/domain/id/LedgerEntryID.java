package com.lucas.pacioli_ledger.shared.domain.id;

import java.util.Objects;
import java.util.UUID;

public record LedgerEntryID(UUID id) {

    public LedgerEntryID {
        Objects.requireNonNull(id, "LedgerEntryID cannot be null");
    }

    public static LedgerEntryID generate(){
        return new LedgerEntryID(UUID.randomUUID());
    }
}
