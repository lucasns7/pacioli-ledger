package com.lucas.pacioli_ledger.identity.infra.persistence;

import com.lucas.pacioli_ledger.identity.domain.User;
import com.lucas.pacioli_ledger.identity.domain.UserRepository;
import com.lucas.pacioli_ledger.shared.domain.id.UserID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserRepositoryImpl implements UserRepository {

    private final UserEntityRepository repository;

    @Override
    public void save(User user) {
        repository.save(UserEntity.from(user));
    }

    @Override
    public Optional<UserID> findIdByIdentitySubject(String identitySubject) {
        return repository.findIdByIdentitySubject(identitySubject)
                .map(UserID::new);
    }
}
