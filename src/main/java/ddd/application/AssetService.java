package ddd.application;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import ddd.infrastructure.actor.Actor;
import ddd.infrastructure.audit.AuditHandler;
import ddd.infrastructure.lock.IdLockHandler;
import ddd.infrastructure.orm.OrmRepository.DefaultRepository;
import ddd.infrastructure.orm.TxTemplate;
import ddd.model.asset.CashInOut;
import ddd.model.asset.CashInOut.RegCashOut;
import ddd.application.event.AppMailEvent;
import ddd.application.event.type.AppMailType;

/**
 * The customer use case processing for the asset domain.
 */
@Service
@RequiredArgsConstructor
public class AssetService {
    private final DefaultRepository rep;
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;
    private final IdLockHandler idLock;
    private final ApplicationEventPublisher event;

    public List<CashInOut> findUnprocessedCashOut() {
        final String accId = actor().id();
        return TxTemplate.of(txm).readOnly().readIdLock(idLock, accId).tx(() -> {
            return CashInOut.findUnprocessed(rep, accId);
        });
    }

    private Actor actor() {
        return rep.dh().actor();
    }

    public String withdraw(final RegCashOut p) {
        return audit.audit("Requesting a withdrawal", () -> {
            // low: Take account ID lock (WRITE) and transaction and handle transfer
            CashInOut cio = TxTemplate.of(txm).writeIdLock(idLock, actor().id()).tx(() -> {
                return CashInOut.withdraw(rep, p);
            });
            // low: this service e-mail it and notify user.
            this.event.publishEvent(AppMailEvent.of(AppMailType.FINISH_REQUEST_WITHDRAW, cio));
            return cio.getId();
        });
    }

}
