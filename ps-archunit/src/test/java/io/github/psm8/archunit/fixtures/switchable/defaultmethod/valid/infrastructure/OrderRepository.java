package io.github.psm8.archunit.fixtures.switchable.defaultmethod.valid.infrastructure;

public interface OrderRepository {
	String findById(String id);

	default String getOrThrow(String id) {
		String found = findById(id);
		if (found == null) {
			throw new IllegalStateException(id);
		}
		return found;
	}
}
