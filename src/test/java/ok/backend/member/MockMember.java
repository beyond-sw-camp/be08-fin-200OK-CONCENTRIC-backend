package ok.backend.member;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomSecurityContextFactory.class)
public @interface MockMember {
    long id() default 1L;
    String name() default "test";
    String password() default "password";
    String nickname() default "nickname";
    String email() default "test@test.com";

}
