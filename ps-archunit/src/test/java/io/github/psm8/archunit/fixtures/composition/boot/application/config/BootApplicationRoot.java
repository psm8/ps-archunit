package io.github.psm8.archunit.fixtures.composition.boot.application.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@SpringBootApplication
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BootApplicationRoot {
}
