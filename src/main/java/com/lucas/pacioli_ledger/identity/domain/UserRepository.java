package com.lucas.pacioli_ledger.identity.domain;

import com.lucas.pacioli_ledger.shared.domain.id.UserID;

import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<UserID> findIdByIdentitySubject(String identitySubject);
}
