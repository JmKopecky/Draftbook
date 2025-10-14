package dev.jkopecky.draftbook.draftbook.data;

import dev.jkopecky.draftbook.draftbook.exceptions.NonexistentAccountException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Optional;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String oidcIdentifier;

    /**
     * Retrieve an account from an oidc identifier. If it doesn't already exist in the database, add it.
     * @param oidcIdentifier The identifier for the user, derived by OidcUser.getSubject().
     * @param accountRepository The account table in the database.
     * @return An Account object representing the user's account.
     */
    public static Account getOrCreateAccount(String oidcIdentifier, AccountRepository accountRepository) {
        Optional<Account> existingAccount = accountRepository.findByOidcIdentifier(oidcIdentifier);

        //if the account already exists, return it
        if (existingAccount.isPresent()) {
            return existingAccount.get();
        }

        //otherwise, the account doesn't exist yet, so we need to make it.
        Account newAccount = new Account();
        newAccount.setOidcIdentifier(oidcIdentifier);
        accountRepository.save(newAccount);
        return newAccount;
    }

    public String getOidcIdentifier() {
        return oidcIdentifier;
    }
    public void setOidcIdentifier(String oidcIdentifier) {
        this.oidcIdentifier = oidcIdentifier;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
