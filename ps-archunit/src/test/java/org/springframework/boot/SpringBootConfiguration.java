package org.springframework.boot;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Configuration(proxyBeanMethods = false)
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringBootConfiguration {
}
