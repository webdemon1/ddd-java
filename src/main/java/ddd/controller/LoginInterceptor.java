package ddd.controller;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import ddd.infrastructure.actor.Actor;
import ddd.infrastructure.actor.ActorRoleType;
import ddd.infrastructure.actor.ActorSession;

/**
 * AOPInterceptor relates a login user with thread local.
 * low: It is a dummy because no authentication function is provided.
 */
@Aspect
@Component
public class LoginInterceptor {

    @Before("execution(* ddd.controller.*Controller.*(..))")
    public void bindUser() {
        ActorSession.bind(Actor.builder()
                .id("ddd")
                .name("ddd")
                .roleType(ActorRoleType.USER)
                .build());
    }

    @Before("execution(* ddd.controller.admin.*Controller.*(..))")
    public void bindAdmin() {
        ActorSession.bind(Actor.builder()
                .id("admin")
                .name("admin")
                .roleType(ActorRoleType.INTERNAL)
                .build());
    }

    @Before("execution(* ddd.controller.system.*Controller.*(..))")
    public void bindSystem() {
        ActorSession.bind(Actor.System);
    }

    @After("execution(* ddd.controller..*Controller.*(..))")
    public void unbind() {
        ActorSession.unbind();
    }

}
