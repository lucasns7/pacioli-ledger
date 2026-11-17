package com.lucas.pacioli_ledger.shared.domain.id;

import java.util.Objects;
import java.util.UUID;

public record LedgerTransactionID(UUID id) {

    public LedgerTransactionID {
        Objects.requireNonNull(id, "LedgerTransactionID cannot be null");
    }

    public static LedgerTransactionID generate(){
        return new LedgerTransactionID(UUID.randomUUID());
    }
}
