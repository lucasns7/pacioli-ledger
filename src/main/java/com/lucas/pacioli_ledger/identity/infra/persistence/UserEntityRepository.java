package com.lucas.pacioli_ledger.identity.infra.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserEntityRepository extends CrudRepository<UserEntity, UUID> {

    @Query("SELECT u.id FROM UserEntity u WHERE u.identitySubject = :identitySubject")
    Optional<UUID> findIdByIdentitySubject(@Param("identitySubject") String identitySubject);
}
