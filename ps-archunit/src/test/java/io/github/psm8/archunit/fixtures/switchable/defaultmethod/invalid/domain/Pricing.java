package io.github.psm8.archunit.fixtures.switchable.defaultmethod.invalid.domain;

public interface Pricing {
	long base();

	default long withTax() {
		return base() * 123 / 100;
	}
}
