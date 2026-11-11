package io.github.psm8.archunit.fixtures.sharedlib.coreuse.application.service;

import io.github.psm8.archunit.fixtures.sharedlib.coreuse.lib.RequestTag;

public final class ItemService {
	public String tag() {
		return RequestTag.of("application");
	}
}
