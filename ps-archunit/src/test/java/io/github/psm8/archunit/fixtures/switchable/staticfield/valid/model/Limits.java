package io.github.psm8.archunit.fixtures.switchable.staticfield.valid.model;

public class Limits {
	static final int MAX = 10;
	private final int current;

	Limits(int current) {
		this.current = current;
	}

	boolean exceeded() {
		assert current >= 0;
		return current > MAX;
	}
}
