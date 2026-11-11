package io.github.psm8.archunit.fixtures.switchable.defaultmethod.valid.domain;

public sealed interface Money permits Money.Euro {
	long cents();

	default boolean isPositive() {
		return cents() > 0;
	}

	record Euro(long cents) implements Money {
	}
}
