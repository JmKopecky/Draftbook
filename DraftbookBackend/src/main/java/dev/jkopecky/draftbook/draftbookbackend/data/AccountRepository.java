package dev.jkopecky.draftbook.draftbook.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Integer> {
    Optional<Account> findByOidcIdentifier(String identifier);
}
