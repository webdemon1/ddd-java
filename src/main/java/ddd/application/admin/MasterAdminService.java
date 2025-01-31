package ddd.application.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import ddd.infrastructure.Timestamper;
import ddd.infrastructure.audit.AuditHandler;
import ddd.infrastructure.orm.OrmRepository.DefaultRepository;

/**
 * The use case processing for the master domain in the organization.
 */
@Service
@RequiredArgsConstructor
public class MasterAdminService {
    private final DefaultRepository rep;
    @SuppressWarnings("unused")
    private final PlatformTransactionManager txm;
    private final AuditHandler audit;

    public void processDay() {
        audit.audit("Forward day.", () -> {
            Timestamper time = rep.dh().time();
            time.daySet(time.dayPlus(1));
        });
    }

}
