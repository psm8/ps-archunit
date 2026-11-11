package io.github.psm8.archunit.fixtures.sharedlib.coreuse.domain.model;

import io.github.psm8.archunit.fixtures.sharedlib.coreuse.lib.RequestTag;

public record Item(String id) {
	public static Item tagged() {
		return new Item(RequestTag.of("domain"));
	}
}
