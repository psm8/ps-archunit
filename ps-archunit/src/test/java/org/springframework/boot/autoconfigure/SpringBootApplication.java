package org.springframework.boot.autoconfigure;

import org.springframework.boot.SpringBootConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@SpringBootConfiguration
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringBootApplication {
}
