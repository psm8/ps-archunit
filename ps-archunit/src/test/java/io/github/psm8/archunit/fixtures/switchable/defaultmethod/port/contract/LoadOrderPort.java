package io.github.psm8.archunit.fixtures.switchable.defaultmethod.port.contract;

public interface LoadOrderPort {
	String load(String id);

	default String loadOrEmpty(String id) {
		String loaded = load(id);
		return loaded == null ? "" : loaded;
	}
}
