package io.github.psm8.archunit.fixtures.switchable.defaultmethod.valid.application;

public interface OrderLookup {
	String find(String id);

	static String unknown() {
		return "unknown";
	}
}
