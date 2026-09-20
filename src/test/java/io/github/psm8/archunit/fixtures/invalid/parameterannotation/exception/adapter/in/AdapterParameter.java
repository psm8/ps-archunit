package io.github.psm8.archunit.fixtures.invalid.parameterannotation.exception.adapter.in;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

@Target(PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdapterParameter {
}
