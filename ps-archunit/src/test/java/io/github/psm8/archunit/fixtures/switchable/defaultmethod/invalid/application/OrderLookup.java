package io.github.psm8.archunit.fixtures.switchable.defaultmethod.invalid.application;

public interface OrderLookup {
	String find(String id);

	default String findOrUnknown(String id) {
		String found = find(id);
		return found == null ? "unknown" : found;
	}
}
