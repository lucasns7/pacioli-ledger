package com.lucas.pacioli_ledger.shared.domain.id;

import java.util.Objects;
import java.util.UUID;

public record WalletID(UUID id) {

    public WalletID {
        Objects.requireNonNull(id, "WalletID cannot be null");
    }

    public static WalletID generate(){
        return new WalletID(UUID.randomUUID());
    }
}
