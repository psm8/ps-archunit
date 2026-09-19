package org.springframework.stereotype;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Component
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
}
