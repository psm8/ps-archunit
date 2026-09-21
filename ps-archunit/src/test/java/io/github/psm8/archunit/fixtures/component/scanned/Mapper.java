package io.github.psm8.archunit.fixtures.component.scanned;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import static java.lang.annotation.ElementType.TYPE;

@Component
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapper {
}
