package com.lucas.pacioli_ledger.shared.domain.id;

import java.util.Objects;
import java.util.UUID;

public record TransferID(UUID id) {

    public TransferID {
        Objects.requireNonNull(id, "TransferID cannot be null");
    }

    public static TransferID generate(){
        return new TransferID(UUID.randomUUID());
    }
}
