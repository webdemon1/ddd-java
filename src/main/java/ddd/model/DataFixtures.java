package ddd.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ddd.model.asset.ActionStatusType;
import ddd.infrastructure.Timestamper;
import ddd.infrastructure.actor.Actor;
import ddd.infrastructure.orm.OrmRepository;
import ddd.infrastructure.orm.TxTemplate;
import ddd.model.account.Account;
import ddd.model.account.Account.AccountStatusType;
import ddd.model.account.FiAccount;
import ddd.model.asset.CashBalance;
import ddd.model.asset.CashInOut;
import ddd.model.asset.Cashflow;
import ddd.model.asset.Cashflow.CashflowType;
import ddd.model.asset.Cashflow.RegCashflow;
import ddd.model.asset.Remarks;
import ddd.model.master.SelfFiAccount;
import ddd.common.TimePoint;

/**
 * A support component for the data generation.
 * <p>
 * It is aimed for master data generation at the time of a test and the
 * development,
 * Please do not use it in the production.
 */
@Component
@RequiredArgsConstructor(staticName = "of")
public class DataFixtures {
    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    private final Timestamper time;

    @PostConstruct
    public void initialize() {
        TxTemplate.of(txm).tx(() -> {
            initializeInTx();
            return null;
        });
    }

    public void initializeInTx() {
        String ccy = "JPY";
        LocalDate baseDay = LocalDate.now();

        time.daySet(baseDay);

        this.rep.save(selfFiAcc(Remarks.CASH_OUT, ccy));

        // Account: sample
        String idSample = "ddd";
        this.rep.save(acc(idSample));
        this.rep.save(fiAcc(idSample, Remarks.CASH_OUT, ccy));
        this.rep.save(cb(idSample, baseDay, ccy, "1000000"));
    }

    // account

    public static Account acc(String id) {
        var m = new Account();
        m.setId(id);
        m.setName(id);
        m.setMail("hoge@example.com");
        m.setStatusType(AccountStatusType.NORMAL);
        return m;
    }

    public static FiAccount fiAcc(String accountId, String category, String currency) {
        var m = new FiAccount();
        m.setAccountId(accountId);
        m.setCategory(category);
        m.setCurrency(currency);
        m.setFiCode(category + "-" + currency);
        m.setFiAccountId("FI" + accountId);
        return m;
    }

    // asset

    public static CashBalance cb(String accountId, LocalDate baseDay, String currency, String amount) {
        var m = new CashBalance();
        m.setAccountId(accountId);
        m.setBaseDay(baseDay);
        m.setCurrency(currency);
        m.setAmount(new BigDecimal(amount));
        m.setUpdateDate(LocalDateTime.now());
        return m;
    }

    public static Cashflow cf(String accountId, String amount, LocalDate eventDay, LocalDate valueDay) {
        return cfReg(accountId, amount, valueDay).create(TimePoint.of(eventDay), Actor.Anonymous.id());
    }

    public static RegCashflow cfReg(String accountId, String amount, LocalDate valueDay) {
        return RegCashflow.builder()
                .accountId(accountId)
                .currency("JPY")
                .amount(new BigDecimal(amount))
                .cashflowType(CashflowType.CASH_IN)
                .remark("cashIn")
                .valueDay(valueDay)
                .build();
    }

    // eventDay(T+1) / valueDay(T+3)
    public static CashInOut cio(
            String id, String accountId, String absAmount, boolean withdrawal, TimePoint now) {
        var cb = new CashInOut();
        cb.setId(id);
        cb.setAccountId(accountId);
        cb.setCurrency("JPY");
        cb.setAbsAmount(new BigDecimal(absAmount));
        cb.setWithdrawal(withdrawal);
        cb.setRequestDay(now.getDay());
        cb.setRequestDate(now.getDate());
        cb.setEventDay(now.getDay().plusDays(1));
        cb.setValueDay(now.getDay().plusDays(3));
        cb.setTargetFiCode("tFiCode");
        cb.setTargetFiAccountId("tFiAccId");
        cb.setSelfFiCode("sFiCode");
        cb.setSelfFiAccountId("sFiAccId");
        cb.setStatusType(ActionStatusType.UNPROCESSED);
        cb.setUpdateActor("dummy");
        cb.setUpdateDate(now.getDate());
        cb.setCashflowId(null);
        return cb;
    }

    // master

    public static SelfFiAccount selfFiAcc(String category, String currency) {
        var m = new SelfFiAccount();
        m.setCategory(category);
        m.setCurrency(currency);
        m.setFiCode(category + "-" + currency);
        m.setFiAccountId("xxxxxx");
        return m;
    }

}
