package ddd.model.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ddd.infrastructure.DomainEntity;
import ddd.infrastructure.ValidationException;
import ddd.infrastructure.orm.OrmRepository;
import ddd.model.constraints.AccountId;
import ddd.model.constraints.Email;
import ddd.model.constraints.Name;

/**
 * Account.
 * low: The minimum columns with this sample.
 */
@Entity
@Data
public class Account implements DomainEntity {

    @Id
    @AccountId
    private String id;
    @Name
    private String name;
    @Email
    private String mail;
    @NotNull
    @Enumerated
    private AccountStatusType statusType;

    public static Account load(final OrmRepository rep, String id) {
        return rep.load(Account.class, id);
    }

    public static Account loadActive(final OrmRepository rep, String id) {
        Account acc = load(rep, id);
        if (acc.getStatusType().inacitve()) {
            throw new ValidationException("error.Account.loadActive");
        }
        return acc;
    }

    public static enum AccountStatusType {
        NORMAL,
        WITHDRAWAL;

        public boolean inacitve() {
            return this == WITHDRAWAL;
        }
    }

}
