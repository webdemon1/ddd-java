package ddd.application.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ddd.infrastructure.audit.AuditHandler;
import ddd.infrastructure.lock.IdLockHandler;
import ddd.infrastructure.lock.IdLockHandler.LockType;
import ddd.infrastructure.orm.OrmRepository.DefaultRepository;
import ddd.infrastructure.orm.TxTemplate;
import ddd.model.asset.CashInOut;
import ddd.model.asset.CashInOut.FindCashInOut;
import ddd.model.asset.Cashflow;

/**
 * The use case processing for the asset domain in the organization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetAdminService {

    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final IdLockHandler idLock;

    public List<CashInOut> findCashInOut(final FindCashInOut p) {
        return TxTemplate.of(txm).readOnly().tx(() -> {
            return CashInOut.find(rep, p);
        });
    }

    public void closingCashOut() {
        audit.audit("Closing cash out.", () -> {
            TxTemplate.of(txm).tx(() -> {
                closingCashOutInTx();
            });
        });
    }

    private void closingCashOutInTx() {
        // low: It is desirable to handle it to an account unit in a mass.
        // low: Divide paging by id sort and carry it out for a difference
        // because heaps overflow when just do it in large quantities.
        CashInOut.findUnprocessed(rep).forEach(cio -> {
            idLock.call(cio.getAccountId(), LockType.WRITE, () -> {
                try {
                    cio.process(rep);
                    // low: Guarantee that SQL is carried out.
                    rep.flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cio.getId() + "] Failure closing cash out.", e);
                    try {
                        cio.error(rep);
                        rep.flush();
                    } catch (Exception ex) {
                        // low: Keep it for a mention only for logger which is a double obstacle.
                        // (probably DB is caused)
                    }
                }
            });
        });
    }

    /**
     * Reflect the cashflow that reached an account day in the balance.
     */
    public void realizeCashflow() {
        audit.audit("Realize cashflow.", () -> {
            TxTemplate.of(txm).tx(() -> realizeCashflowInTx());
        });
    }

    private void realizeCashflowInTx() {
        // low: Expect the practice after the rollover day.
        var day = rep.dh().time().day();
        for (final Cashflow cf : Cashflow.findDoRealize(rep, day)) {
            idLock.call(cf.getAccountId(), LockType.WRITE, () -> {
                try {
                    cf.realize(rep);
                    rep.flushAndClear();
                } catch (Exception e) {
                    log.error("[" + cf.getId() + "] Failure realize cashflow.", e);
                    try {
                        cf.error(rep);
                        rep.flush();
                    } catch (Exception ex) {
                    }
                }
                return null;
            });
        }
    }

}
