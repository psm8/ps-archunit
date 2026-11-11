package io.github.psm8.archunit.fixtures.switchable.staticfield.invalid.model;

public class Counter {
	static int count;

	static void increment() {
		count++;
	}
}
