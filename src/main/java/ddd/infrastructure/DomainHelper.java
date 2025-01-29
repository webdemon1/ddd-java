package ddd.infrastructure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ddd.infrastructure.actor.Actor;
import ddd.infrastructure.actor.ActorSession;
import ddd.infrastructure.spring.ObjectProviderAccessor;
import ddd.infrastructure.uid.IdGenerator;

/**
 * The access to the domain infrastructure layer component which is necessary in
 * handling it.
 */
public interface DomainHelper {

    /** Return a login user. */
    default Actor actor() {
        return ActorSession.actor();
    }

    Timestamper time();

    IdGenerator uid();

    @Component
    @RequiredArgsConstructor
    public static class DomainHelperImpl implements DomainHelper {
        private final ObjectProvider<Timestamper> time;
        private final ObjectProvider<IdGenerator> uid;
        private final ObjectProviderAccessor accessor;

        @Override
        public Timestamper time() {
            return this.accessor.bean(this.time, Timestamper.class);
        }

        @Override
        public IdGenerator uid() {
            return this.accessor.bean(this.uid, IdGenerator.class);
        }
    }

}
